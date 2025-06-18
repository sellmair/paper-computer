package me.semoro.papercpu

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.collections.get

@Composable
@Preview
fun App() {
    val viewModel = remember { SimulatorViewModel() }

    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Top row: Registers and Memory Grid
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Registers panel
                    RegistersPanel(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.4f)
                    )

                    // Memory grid
                    MemoryGrid(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom row: Program ROM and I/O + Controls
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Program ROM panel
                    ProgramRomPanel(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.5f)
                    )

                    // I/O and Controls panel
                    IoControlsPanel(
                        viewModel = viewModel,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
//            // Draw arrow overlay if both read and write pointers are set
//            if (readPointer != null && writePointer != null &&
//                readPointer != writePointer &&
//                cellPositions.containsKey(readPointer) &&
//                cellPositions.containsKey(writePointer)) {

            val editingPos by viewModel.editingCellAddress.collectAsState()
            val rp = viewModel.readPointer.collectAsState()
            val wp = viewModel.writePointer.collectAsState()
            val pcp by viewModel.pcPointer.collectAsState()

            val cellPositions = viewModel.cellUiNodePosition

            if (editingPos == null) {
                // Draw arrow overlay between read and write pointers
                if (cellPositions.containsKey(rp.value) && cellPositions.containsKey(wp.value)) {

                    val from = animateOffsetAsState(cellPositions[rp.value]!!.readNodeOffset)
                    val to = animateOffsetAsState(cellPositions[wp.value]!!.writeNodeOffset)

                    ArrowOverlay(
                        startPosition = from.value,
                        endPosition = to.value,
                        arrowColor = Color.Blue.copy(alpha = 0.7f),
                        arrowWidth = 5f,
                        arrowHeadSize = 20f
                    )
                }


                // Draw box overlay around the PC
                if (cellPositions.containsKey(pcp)) {
                    val targetRect = animateRectAsState(cellPositions[pcp]!!.pcNodeRect)

                    PCOverlay(
                        rect = targetRect.value
                    )
                }
            } else {

                val memory by viewModel.memory.collectAsState()
                val (rpx, wpx) = decodeInstruction(memory[editingPos!!])

                if (cellPositions.containsKey(rpx) && cellPositions.containsKey(wpx)) {
                    editingPos
                    val from = animateOffsetAsState(cellPositions[rpx]!!.readNodeOffset)
                    val to = animateOffsetAsState(cellPositions[wpx]!!.writeNodeOffset)

                    ArrowOverlay(
                        startPosition = from.value,
                        endPosition = to.value,
                        arrowColor = Color.Gray.copy(alpha = 0.7f),
                        arrowWidth = 5f,
                        arrowHeadSize = 20f
                    )
                }
            }
//            }
        }
    }
}
