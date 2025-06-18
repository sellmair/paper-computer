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
                text = "Program ROM (@50-@99)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ControlRow(viewModel)
            InstructionInfo(viewModel)

            val memory by viewModel.memory.collectAsState()
            // Program ROM editor
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(memory.mapIndexed { idx, v -> idx to v }.takeLast(50)) { (address, value1) ->
                    val src = value1 / 100
                    val dst = value1 % 100
                    RowValueCard(
                        value = value1,
                        isReadFrom = address == readPointer,
                        isWrittenTo = address == writePointer,
                        isRunning = isRunning,
                        onValueChange = { newValue: Int ->
                            if (!isRunning) {
                                viewModel.updateMemory(address, newValue)
                            }
                        },
                        onChangeEditState = { bool: Boolean ->
                            if (bool) {
                                viewModel.setEditingActive(address)
                            } else {
                                viewModel.setEditingActive(null)
                            }
                        },
                        // Breakpoint properties
                        isBreakpoint = viewModel.hasBreakpoint(address),
                        onToggleBreakpoint = { addr: Int -> viewModel.toggleBreakpoint(addr) },
                        address = address,
                        isEditable = true,
                        additionalContent = {
                            // Decode preview
                            Text(
                                text = "@${src.toString().padStart(2, '0')} → @${dst.toString().padStart(2, '0')}",
                                modifier = Modifier.padding(start = 25.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier,
                        valueCellNodePositionContainer = viewModel
                    )
                }
            }
        }
    }
}
