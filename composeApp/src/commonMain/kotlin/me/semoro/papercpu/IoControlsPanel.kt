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

@Composable
fun IoControlsPanel(viewModel: SimulatorViewModel, modifier: Modifier = Modifier) {

    val isRunning by viewModel.isRunning.collectAsState()
    val outputLog by viewModel.outputLog.collectAsState()

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            ControlRow(isRunning, viewModel, modifier)


            // Output log
            Text(
                text = "Output Log:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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

@Composable
private fun ControlRow(
    isRunning: Boolean,
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    // Controls - all buttons in a single row
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
            Text("|<")
        }

        IconButton(
            onClick = { viewModel.step() },
            modifier = Modifier.weight(1f)
        ) {
           Text(">|")
        }

        IconButton(
            onClick = { viewModel.toggleRun() },
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isRunning) "⏸️" else "▶️")
        }

        IconButton(
            onClick = { viewModel.reset() },
            enabled = !isRunning,
            modifier = Modifier.weight(1f)
        ) {
            Text("🔄")
        }
    }
}
