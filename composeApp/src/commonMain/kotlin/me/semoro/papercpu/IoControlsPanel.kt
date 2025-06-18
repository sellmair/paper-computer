package me.semoro.papercpu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Refresh
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IoControlsPanel(viewModel: SimulatorViewModel, modifier: Modifier = Modifier) {

    val isHalted by viewModel.isHalted.collectAsState()
    val outputLog by viewModel.outputLog.collectAsState()

    val isWaitingForInput = viewModel.readPointer.collectAsState().value == Simulator.INP

    Card(
        modifier = modifier.fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "I/O and Controls",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input field
            var inputValue by remember { mutableStateOf("") }

            OutlinedTextField(
                value = inputValue,
                onValueChange = { newText ->
                    if (newText.length <= 4 && newText.all { it.isDigit() }) {
                        inputValue = newText
                        if (newText.isNotEmpty()) {
                            viewModel.updateMemory(12, newText.toInt())
                        }
                    }
                },
                label = { Text("Input Value (IN)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isWaitingForInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = if (isWaitingForInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    focusedContainerColor = if (isWaitingForInput) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (isWaitingForInput) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            )




            // Output log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Output Log:",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isHalted) {
                    Text(
                        text = "Program halted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                if (outputLog.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No output yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(outputLog) { value ->
                            Text(
                                text = value.toString().padStart(4, '0'),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



interface SimulationControlViewModel {
    val isRunning: StateFlow<Boolean>
    val isHalted: StateFlow<Boolean>
    fun step()
    fun stepBack(): Boolean
    fun toggleRun()
    fun reset()

    fun clearProgram()
}

@Composable
fun ControlRow(
    viewModel: SimulationControlViewModel,
    modifier: Modifier = Modifier
) {

    val isRunning by viewModel.isRunning.collectAsState()
    val isHalted by viewModel.isHalted.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { viewModel.stepBack() },
            enabled = !isRunning,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Step Back")
        }

        IconButton(
            onClick = { viewModel.step() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Rounded.SkipNext, 
                contentDescription = "Step Forward",
                tint = if (isHalted) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
        }

        IconButton(
            onClick = { viewModel.toggleRun() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isRunning) "Pause" else "Play",
                tint = if (isHalted && !isRunning) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
        }

        IconButton(
            onClick = { viewModel.reset() },
            enabled = !isRunning,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = "Reset")
        }

        IconButton(
            onClick = { viewModel.clearProgram() },
            enabled = !isRunning,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
        }
    }
}

@Composable
@Preview
fun ControlRowPreview() {
    MaterialTheme {
        val previewViewModel = object : SimulationControlViewModel {
            override val isRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
            override val isHalted = kotlinx.coroutines.flow.MutableStateFlow(false)
            override fun step() {}
            override fun stepBack(): Boolean = true
            override fun toggleRun() {}
            override fun reset() {}
            override fun clearProgram() {}
        }

        ControlRow(
            viewModel = previewViewModel,
            modifier = Modifier.padding(16.dp)
        )
    }
}
