package me.semoro.papercpu

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


data class CellNodePositions(
    val readNodeOffset: Offset,
    val writeNodeOffset: Offset,
    val pcNodeRect: Rect,
) {

}

interface ValueCellNodePositionContainer {
    val cellUiNodePosition: SnapshotStateMap<Int, CellNodePositions>
}



/**
 * ViewModel for the MOV-Only Architecture Simulator.
 * Bridges the core simulation logic with the UI.
 */
class SimulatorViewModel: ValueCellNodePositionContainer, SimulationControlViewModel {
    private val simulator = Simulator()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var runJob: Job? = null
    private val storage = getStorage()

    // Expose simulator state
    val memory = simulator.memory
    val readPointer = simulator.readPointer
    val writePointer = simulator.writePointer
    val pcPointer = simulator.pcPointer


    private val _editingCellAddress = MutableStateFlow<Int?>(null)
    val editingCellAddress = _editingCellAddress.asStateFlow()

    fun setEditingActive(cellAddress: Int?) {
        _editingCellAddress.value = cellAddress
    }

    // Simulation state
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _outputLog = MutableStateFlow<List<Int>>(emptyList())
    val outputLog: StateFlow<List<Int>> = _outputLog.asStateFlow()

    override val cellUiNodePosition = mutableStateMapOf<Int, CellNodePositions>()

    // Last output value for change detection
    private var lastOutputValue: Int = 0

    init {
        // Try to load saved program
        coroutineScope.launch {
            val savedMemory = storage.loadProgram()
            if (savedMemory != null) {
                simulator.updateMemoryArray(savedMemory)
            }
        }
    }

    /**
     * Executes a single step of the simulation and saves the program.
     */
    override fun step() {
        simulator.step()
        checkOutputChange()

        // Save the program after step
        saveProgram()
    }

    /**
     * Steps back to the previous state and saves the program.
     * @return true if step back was successful, false if history is empty
     */
    override fun stepBack(): Boolean {
        val result = simulator.stepBack()

        // Save the program after step back (only if successful)
        if (result) {
            saveProgram()
        }

        return result
    }

    /**
     * Starts or stops the automatic execution of the simulation.
     */
    override fun toggleRun() {
        if (_isRunning.value) {
            stopRun()
        } else {
            startRun()
        }
    }

    /**
     * Starts the automatic execution of the simulation.
     */
    private fun startRun() {
        if (runJob != null) return

        runJob = coroutineScope.launch {
            _isRunning.value = true
            var stepCount = 0

            while (isActive) {
                val pc = memory.value[1]
                val instr = memory.value[pc]

                if (instr == 0) {
                    // HALT instruction, stop running
                    stopRun()
                    break
                }

                simulator.step()
                checkOutputChange()

                // Save the program periodically (every 5 steps)
                stepCount++
                if (stepCount % 5 == 0) {
                    saveProgram()
                }

                // Limit execution speed to avoid UI freezing
                delay(100) // 10 steps per second, can be adjusted
            }

            // Save the program when execution stops
            saveProgram()
        }
    }

    /**
     * Stops the automatic execution of the simulation and saves the program.
     */
    fun stopRun() {
        runJob?.cancel()
        runJob = null
        _isRunning.value = false

        // Save the program when execution stops
        saveProgram()
    }

    /**
     * Resets the simulator to its initial state and saves the program.
     */
    override fun reset() {
        stopRun()
        simulator.reset()
        _outputLog.value = emptyList()
        lastOutputValue = 0

        // Save the program after reset
        saveProgram()
    }

    /**
     * Updates a memory cell with a new value and saves the program.
     */
    fun updateMemory(address: Int, value: Int) {
        stopRun() // Stop running when memory is manually edited
        simulator.updateMemory(address, value)

        // Save the program after updating memory
        saveProgram()
    }

    /**
     * Saves the current program to local storage.
     */
    private fun saveProgram() {
        coroutineScope.launch {
            storage.saveProgram(memory.value)
        }
    }

    /**
     * Checks if the output value (memory[11]) has changed and updates the output log.
     */
    private fun checkOutputChange() {
        val currentOutput = memory.value[11]
        if (currentOutput != lastOutputValue) {
            _outputLog.value = _outputLog.value + currentOutput
            lastOutputValue = currentOutput
        }
    }
}
