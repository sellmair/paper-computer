package me.semoro.papercpu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MemoryGrid(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier,
    range: IntRange
) {
    val memory by viewModel.memory.collectAsState()
    val readPointer by viewModel.readPointer.collectAsState()
    val writePointer by viewModel.writePointer.collectAsState()

    // Keep track of cell positions

    Box(modifier = modifier.fillMaxHeight()) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Memory",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Memory grid excluding registers (0-12) and ROM (50-99)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(range.toList()) { address ->
                        Box(
                            modifier = Modifier
                        ) {
                            BoxValueCard(
                                value = memory[address],
                                isPC = address == memory[1],
                                isReadFrom = address == readPointer,
                                isWrittenTo = address == writePointer,
                                address = address,
                                valueCellNodePositionContainer = viewModel
                            )
                        }
                    }
                }
            }
        }


    }
}


