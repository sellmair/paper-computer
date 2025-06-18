package me.semoro.papercpu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


fun setCellNodePositions(coordinates: LayoutCoordinates, container: ValueCellNodePositionContainer, address: Int) {
    // Store the center position of each cell
    val cY = coordinates.size.height / 2f + coordinates.positionInRoot().y

    container.cellUiNodePosition.put(address,
        CellNodePositions(
            Offset(
                coordinates.positionInRoot().x + coordinates.size.width,
                cY,
            ),
            Offset(
                coordinates.positionInRoot().x,
                cY,
            ),
            Rect(
                Offset(
                    coordinates.positionInRoot().x,
                    coordinates.positionInRoot().y
                ),
                Offset(
                    coordinates.positionInRoot().x + coordinates.size.width,
                    coordinates.positionInRoot().y + coordinates.size.height
                )
            )
        )
    )
}
@Composable
fun ValueCardInner(value: Int, address: Int?, modifier: Modifier, cellNodePositionContainer: ValueCellNodePositionContainer?) {
    val valueColor = if (value == 0)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.onSurface

    val valueText = value.toString().padStart(4, '0')

    val modifier = if (cellNodePositionContainer != null) {
        modifier.onGloballyPositioned { coordinates ->
            setCellNodePositions(coordinates, cellNodePositionContainer, address!!)
        }
    } else {
        modifier
    }

    ElevatedCard (
        shape = CardDefaults.elevatedShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = modifier.then(Modifier.widthIn(min = 80.dp))
    ) {
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            maxLines = 1,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(4.dp)
        )
    }
}

/**
 * A common component for displaying value cards throughout the application.
 * This component can be configured to display in different layouts and with different features.
 */
@Composable
fun ValueCard(
    // Common properties
    value: Int, // Can be Int or String
    isPC: Boolean = false,
    isReadFrom: Boolean = false,
    isWrittenTo: Boolean = false,
    isRunning: Boolean = false,
    onValueChange: ((Int) -> Unit)? = null,
    onChangeEditState: ((Boolean) -> Unit)? = null,

    // Identifier properties
    address: Int? = null,
    name: String? = null,

    // Layout and style properties
    layout: ValueCardLayout = ValueCardLayout.BOX,
    isEditable: Boolean = false,
    isDerived: Boolean = false,
    isSpecial: Boolean = false,

    // Additional content
    additionalContent: (@Composable () -> Unit)? = null,

    // Modifiers
    modifier: Modifier = Modifier,
    valueCellNodePositionContainer: ValueCellNodePositionContainer? = null
) {
    val backgroundColor = when {
        isPC -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isPC -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    // Convert value to int and string safely
    val intValue = value

    val valueColor = if (intValue == 0) 
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.onSurface

    val valueText = value.toString().padStart(4, '0')

    when (layout) {
        ValueCardLayout.BOX -> {
            // Box layout (similar to MemoryCell)
            Box(
                modifier = modifier
                    .size(80.dp)
                    .background(backgroundColor)
                    .border(BorderStroke(1.dp, borderColor))
                    .padding(4.dp)
            ) {
                // Address
                if (address != null) {
                    Text(
                        text = "@${address.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }

                // Value
                if (isEditable && onValueChange != null && !isRunning) {
                    var textValue by remember { mutableStateOf(valueText) }

                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { newText ->
                            if (newText.length <= 4 && newText.all { it.isDigit() }) {
                                textValue = newText
                                if (newText.isNotEmpty()) {
                                    onValueChange(newText.toInt())
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                            setCellNodePositions(coordinates, valueCellNodePositionContainer!!, address!!)
                        },
                        enabled = !isRunning,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = valueColor
                        )
                    )
                } else {
                    ValueCardInner(value, address, modifier = Modifier.align(Alignment.Center), valueCellNodePositionContainer)
                }

//                // PC indicator
//                if (isPC) {
//                    Text(
//                        text = "PC",
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.align(Alignment.TopEnd)
//                    )
//                }

                // Read indicator
                if (isReadFrom) {
                    Text(
                        text = "R",
                        color = Color.Green,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                // Write indicator
                if (isWrittenTo) {
                    Text(
                        text = "W",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }

        ValueCardLayout.ROW -> {
            // Row layout (similar to RegisterRow and ProgramRomRow)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name or Address
                if (name != null) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            fontStyle = if (isDerived) FontStyle.Italic else FontStyle.Normal,
                            color = if (isSpecial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

//                        if (isPC) {
//                            Text(
//                                text = " PC",
//                                color = MaterialTheme.colorScheme.primary,
//                                style = MaterialTheme.typography.labelSmall,
//                                modifier = Modifier.padding(start = 4.dp)
//                            )
//                        }
                    }
                } else if (address != null) {
                    Box(
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = "@${address.toString().padStart(2, '0')}",
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

//                        if (isPC) {
//                            Text(
//                                text = "PC",
//                                color = MaterialTheme.colorScheme.primary,
//                                style = MaterialTheme.typography.labelSmall,
//                                modifier = Modifier.align(Alignment.CenterEnd)
//                            )
//                        }
                    }
                }

                // Address (if name is provided)
                if (name != null && address != null) {
                    Text(
                        text = "@${address.toString().padStart(2, '0')}",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Value
                Box(
                    modifier = Modifier
                        .width(if (isEditable) 120.dp else 80.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    if (isEditable && onValueChange != null && !isRunning) {
                        var textValue by remember { mutableStateOf(valueText) }

                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { newText ->
                                if (newText.length <= 4 && newText.all { it.isDigit() }) {
                                    textValue = newText
                                    if (newText.isNotEmpty()) {
                                        onValueChange(newText.toInt())
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                                setCellNodePositions(coordinates, valueCellNodePositionContainer!!, address!!)
                            }.onFocusEvent { evt ->
                                if (onChangeEditState != null) {
                                    onChangeEditState(evt.hasFocus)
                                }
                            },
                            enabled = !isRunning,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = valueColor
                            ),

                        )
                    } else {
                        ValueCardInner(value, address, Modifier.align(Alignment.Center), valueCellNodePositionContainer)
                    }

                    // Read indicator
                    if (isReadFrom) {
                        Text(
                            text = "R",
                            color = Color.Green,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(if (isEditable) Alignment.CenterEnd else Alignment.CenterEnd)
                                .padding(end = if (isEditable) 8.dp else 0.dp)
                        )
                    }

                    // Write indicator
                    if (isWrittenTo) {
                        Text(
                            text = "W",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(if (isEditable) Alignment.CenterStart else Alignment.CenterStart)
                                .padding(start = if (isEditable) 8.dp else 0.dp)
                        )
                    }
                }

                // Additional content (e.g., decode preview)
                if (additionalContent != null) {
                    additionalContent()
                }
            }
        }
    }
}

enum class ValueCardLayout {
    BOX,    // Box layout like MemoryCell
    ROW     // Row layout like RegisterRow and ProgramRomRow
}
