package me.semoro.papercpu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
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
 * Box layout implementation of ValueCard (similar to MemoryCell)
 */
@Composable
fun BoxValueCard(
    // Common properties
    value: Int,
    isPC: Boolean = false,
    isReadFrom: Boolean = false,
    isWrittenTo: Boolean = false,

    // Breakpoint properties
    isBreakpoint: Boolean = false,
    onToggleBreakpoint: ((Int) -> Unit)? = null,
    isAtBreakpoint: Boolean = false,

    // Identifier properties
    address: Int? = null,

    // Modifiers
    modifier: Modifier = Modifier,
    valueCellNodePositionContainer: ValueCellNodePositionContainer? = null
) {
    val backgroundColor = when {
        isBreakpoint && isPC -> Color.Yellow.copy(alpha = 0.3f) // PC at breakpoint
        isPC && isAtBreakpoint -> Color.Yellow.copy(alpha = 0.3f) // PC at breakpoint
        isPC -> MaterialTheme.colorScheme.primaryContainer
        isBreakpoint -> Color.Red.copy(alpha = 0.1f) // Breakpoint
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isBreakpoint && isPC -> Color.Yellow // PC at breakpoint
        isPC && isAtBreakpoint -> Color.Yellow // PC at breakpoint
        isPC -> MaterialTheme.colorScheme.primary
        isBreakpoint -> Color.Red // Breakpoint
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val intValue = value
    val valueColor = if (intValue == 0) 
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.onSurface

    val valueText = value.toString().padStart(4, '0')

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
            val addressText = "@${address.toString().padStart(2, '0')}"
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .let {
                        if (onToggleBreakpoint != null) {
                            it.clickable { onToggleBreakpoint(address) }
                        } else {
                            it
                        }
                    },

            ) {
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        ValueCardInner(value, address, modifier = Modifier.align(Alignment.Center), valueCellNodePositionContainer)


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

/**
 * Row layout implementation of ValueCard (similar to RegisterRow and ProgramRomRow)
 */
@Composable
fun RowValueCard(
    // Common properties
    value: Int,
    isReadFrom: Boolean = false,
    isWrittenTo: Boolean = false,
    isRunning: Boolean = false,
    onValueChange: ((Int) -> Unit)? = null,
    onChangeEditState: ((Boolean) -> Unit)? = null,

    // Breakpoint properties
    isBreakpoint: Boolean = false,
    onToggleBreakpoint: ((Int) -> Unit)? = null,
    isAtBreakpoint: Boolean = false,

    // Identifier properties
    address: Int? = null,
    name: String? = null,

    // Layout and style properties
    isEditable: Boolean = false,
    isDerived: Boolean = false,
    isSpecial: Boolean = false,

    // Additional content
    additionalContent: (@Composable () -> Unit)? = null,

    // Modifiers
    modifier: Modifier = Modifier,
    valueCellNodePositionContainer: ValueCellNodePositionContainer? = null
) {
    val intValue = value
    val valueColor = if (intValue == 0) 
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.onSurface

    val valueText = value.toString().padStart(4, '0')

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
            }
        } else if (address != null) {
            Box(
                modifier = Modifier.width(50.dp)
            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .let {
                            if (onToggleBreakpoint != null) {
                                it.clickable { onToggleBreakpoint(address) }
                            } else {
                                it
                            }
                        }
                ) {
                    val addressText = address.toString().padStart(2, '0')
                    if (isBreakpoint) {
                        Row {
                            Icon(
                                Icons.Filled.PauseCircle,
                                "Unset breakpoint",
                                tint = Color(0xFFC6650A)
                            )
                            Text(
                                text = addressText,
                                modifier = Modifier
                            )
                        }

                    } else {
                        Text(
                            text = "@${addressText}",
                            modifier = Modifier
                        )
                    }
                }
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
            if (isEditable && onValueChange != null) {
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
                    )
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

