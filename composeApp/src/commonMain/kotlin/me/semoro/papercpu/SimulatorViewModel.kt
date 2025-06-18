package me.semoro.papercpu

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


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

    val programDataReloadCounter = simulator.programDataReloadCounter


    private val _editingCellAddress = MutableStateFlow<Int?>(null)
    val editingCellAddress = _editingCellAddress.asStateFlow()

    fun setEditingActive(cellAddress: Int?) {
        _editingCellAddress.value = cellAddress
    }

    // Simulation state
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isHalted = MutableStateFlow(false)
    override val isHalted: StateFlow<Boolean> = _isHalted.asStateFlow()

    private val _outputLog = MutableStateFlow<List<Int>>(emptyList())
    val outputLog: StateFlow<List<Int>> = _outputLog.asStateFlow()

    override val cellUiNodePosition = mutableStateMapOf<Int, CellNodePositions>()

    // Breakpoint state
    val breakpoints = mutableStateSetOf<Int>()

    /**
     * Toggles a breakpoint at the specified address.
     * @param address The address to toggle the breakpoint at
     * @return true if a breakpoint was added, false if it was removed
     */
    fun toggleBreakpoint(address: Int): Boolean {
        return if (breakpoints.contains(address)) {
            breakpoints.remove(address)
            false
        } else {
            breakpoints.add(address)
            true
        }
    }

    /**
     * Checks if an address has a breakpoint.
     * @param address The address to check
     * @return true if the address has a breakpoint, false otherwise
     */
    fun hasBreakpoint(address: Int): Boolean {
        return breakpoints.contains(address)
    }


    init {
        // Try to load saved program
        coroutineScope.launch {
            val savedMemory = storage.loadProgram()
            if (savedMemory != null) {
                simulator.reset()
                simulator.updateProgramData(savedMemory)
            }
        }
        coroutineScope.launch {
            simulator.output.filterNotNull().collect {
                println("Collect: $it")
                val prev = _outputLog.value
                _outputLog.value = prev + it
            }
        }
    }

    /**
     * Executes a single step of the simulation and saves the program.
     */
    override fun step() {
        _isHalted.value = false

        val pc = memory.value[Simulator.PC]
        val instr = memory.value[pc]
        if (instr == 0) {
            // HALT instruction, stop running
            _isHalted.value = true
            return
        }
        simulator.step()

        // Save the program after step
        saveProgram()
    }

    /**
     * Steps back to the previous state and saves the program.
     * @return true if step back was successful, false if history is empty
     */
    override fun stepBack(): Boolean {
        _isHalted.value = false
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

            while (isActive) {
                run {
                    val pc = memory.value[Simulator.PC]
                    val instr = memory.value[pc]

                    if (instr == 0) {
                        // HALT instruction, stop running
                        _isHalted.value = true
                        stopRun()
                        break
                    }
                }


                simulator.step()

                run {
                    val pc = memory.value[Simulator.PC]
                    val instr = memory.value[pc]

                    // Check if we hit a breakpoint
                    if (breakpoints.contains(pc)) {
                        // Pause at breakpoint
                        stopRun()
                        break
                    }

                    val (src, dst) = decodeInstruction(instr)

                    if (src == Simulator.INP) {
                        stopRun()
                        break
                    }
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

    override fun reset() {
        stopRun()

        simulator.reset()
        _outputLog.value = emptyList()
        _isHalted.value = false

        // Save the program after reset
        saveProgram()
    }

    /**
     * Clear the program
     * Also saves the state before resetting as "before_clear_<counter>"
     */
    override fun clearProgram() {
        val td = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        // Save the state before resetting
        saveProgramAs("before_clear_${td}")

        reset()
        simulator.resetProgram()
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
            storage.saveProgram(simulator.getProgramData())
        }
    }

    /**
     * Saves the current program with a specific name.
     * @param name The name to save the program under
     */
    fun saveProgramAs(name: String) {
        coroutineScope.launch {
            storage.saveProgramAs(name, simulator.getProgramData())
        }
    }

    /**
     * Loads a program with a specific name.
     * @param name The name of the program to load
     */
    fun loadProgramByName(name: String) {
        coroutineScope.launch {
            val savedMemory = storage.loadProgramByName(name)
            if (savedMemory != null) {
                simulator.reset()
                simulator.updateProgramData(savedMemory)
                // Save the program after loading
                saveProgram()
            }
        }
    }

    /**
     * Gets a list of all saved program names.
     * @return A list of saved program names
     */
    suspend fun getSavedProgramNames(): List<String> {
        return storage.getSavedProgramNames()
    }

    /**
     * Deletes a program with a specific name.
     * @param name The name of the program to delete
     */
    suspend fun deleteProgram(name: String) {
        storage.deleteProgram(name)
    }

}
