package me.semoro.papercpu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgramRomPanel(viewModel: SimulatorViewModel,
                    modifier: Modifier = Modifier) {
    val memory by viewModel.memory.collectAsState()
    val readPointer by viewModel.readPointer.collectAsState()
    val writePointer by viewModel.writePointer.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

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
                text = "Program ROM (50-99)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Program ROM editor
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(List(50) { it + 50 }) { address ->
                    val value1 = memory[address]
                    val src = value1 / 100
                    val dst = value1 % 100
                    ValueCard(
                        value = value1,
                        address = address,
                        isPC = address == memory[1],
                        isReadFrom = address == readPointer,
                        isWrittenTo = address == writePointer,
                        isRunning = isRunning,
                        onValueChange = { newValue: Int ->
                                            if (!isRunning) {
                                                viewModel.updateMemory(address, newValue)
                                            }
                                        },
                        layout = ValueCardLayout.ROW,
                        isEditable = true,
                        modifier = Modifier,
                        valueCellNodePositionContainer = viewModel,
                        additionalContent = {
                            // Decode preview
                            Text(
                                text = "$src → $dst",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

