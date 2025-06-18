package me.semoro.papercpu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegistersPanel(viewModel: SimulatorViewModel, modifier: Modifier = Modifier.Companion) {
    val memory by viewModel.memory.collectAsState()
    val readPointer by viewModel.readPointer.collectAsState()
    val writePointer by viewModel.writePointer.collectAsState()

    // Add this line to create a scroll state
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(16.dp)
                .fillMaxSize()
                // Add this line to make the column scrollable
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Registers",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.Companion.padding(bottom = 16.dp)
            )

            // PC
            RegisterRow(
                viewModel = viewModel,
                name = "PC",
                address = 1,
                value = memory[1],
                isSpecial = true,
                isReadFrom = 1 == readPointer,
                isWrittenTo = 1 == writePointer
            )

            HorizontalDivider(
                Modifier.Companion.padding(vertical = 8.dp),
                DividerDefaults.Thickness,
                DividerDefaults.color
            )

            // Operands
            RegisterRow(
                viewModel = viewModel,
                name = "A",
                address = 2,
                value = memory[2],
                isReadFrom = 2 == readPointer,
                isWrittenTo = 2 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "B",
                address = 3,
                value = memory[3],
                isReadFrom = 3 == readPointer,
                isWrittenTo = 3 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "C",
                address = 8,
                value = memory[8],
                isReadFrom = 8 == readPointer,
                isWrittenTo = 8 == writePointer
            )

            HorizontalDivider(
                Modifier.Companion.padding(vertical = 8.dp),
                DividerDefaults.Thickness,
                DividerDefaults.color
            )

            // Derived registers
            RegisterRow(
                viewModel = viewModel,
                name = "A + B",
                address = 4,
                value = memory[4],
                isDerived = true,
                isReadFrom = 4 == readPointer,
                isWrittenTo = 4 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "A - B",
                address = 5,
                value = memory[5],
                isDerived = true,
                isReadFrom = 5 == readPointer,
                isWrittenTo = 5 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "A > B",
                address = 6,
                value = memory[6],
                isDerived = true,
                isReadFrom = 6 == readPointer,
                isWrittenTo = 6 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "A if C else B",
                address = 7,
                value = memory[7],
                isDerived = true,
                isReadFrom = 7 == readPointer,
                isWrittenTo = 7 == writePointer
            )

            HorizontalDivider(
                Modifier.Companion.padding(vertical = 8.dp),
                DividerDefaults.Thickness,
                DividerDefaults.color
            )

            // Other registers
            RegisterRow(
                viewModel = viewModel,
                name = "TMP",
                address = 9,
                value = memory[9],
                isReadFrom = 9 == readPointer,
                isWrittenTo = 9 == writePointer
            )
            RegisterRow(
                viewModel = viewModel,
                name = "GP₁",
                address = 0,
                value = memory[10],
                isReadFrom = 10 == readPointer,
                isWrittenTo = 10 == writePointer
            )
        }
    }
}



@Composable
fun InstructionInfo(viewModel: SimulatorViewModel) {
    val memory by viewModel.memory.collectAsState()
    val pc = memory[1]
    val instr = memory[pc]
    val src = instr / 100
    val dst = instr % 100

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.Companion.padding(8.dp)) {
            Text(
                text = "Current Instruction",
                style = MaterialTheme.typography.titleMedium
            )
            Text("PC: @${pc.toString().padStart(2, '0')}")
            Text("Instruction: ${instr.toString().padStart(4, '0')}")
            Text(
                "Source: @${src.toString().padStart(2, '0')} -> Destination: @${
                    dst.toString().padStart(2, '0')
                }"
            )
        }
    }
}

@Composable
fun RegisterRow(
    viewModel: SimulatorViewModel,
    name: String,
    address: Int,
    value: Int,
    isDerived: Boolean = false,
    isSpecial: Boolean = false,
    isReadFrom: Boolean = false,
    isWrittenTo: Boolean = false
) {
    RowValueCard(
        value = value,
        isReadFrom = isReadFrom,
        isWrittenTo = isWrittenTo,
        address = address,
        name = name,
        isDerived = isDerived,
        isSpecial = isSpecial,
        modifier = Modifier.Companion,
        valueCellNodePositionContainer = viewModel
    )
}
