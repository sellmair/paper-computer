package me.semoro.papercpu

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class InsnDec(
    val r: Int,
    val w: Int
)


fun decodeInstruction(value: Int): InsnDec {
    val src = value / 100 // high two digits
    val dst = value % 100 // low two digits

    return InsnDec(src, dst)
}

/**
 * Core simulation logic for the MOV-Only Architecture Simulator.
 * Implements the memory model, execution phases, and history buffer.
 */
class Simulator {
    // Memory array with 100 cells (addresses 00-99)
    private val _memory = MutableStateFlow(IntArray(100))
    val memory: StateFlow<IntArray> = _memory.asStateFlow()

    // Current read and write pointers for visualization
    private val _readPointer = MutableStateFlow<Int?>(null)
    val readPointer: StateFlow<Int?> = _readPointer.asStateFlow()

    private val _writePointer = MutableStateFlow<Int?>(null)
    val writePointer: StateFlow<Int?> = _writePointer.asStateFlow()

    private val _pcPointer = MutableStateFlow<Int?>(null)
    val pcPointer: StateFlow<Int?> = _pcPointer.asStateFlow()

    val output = MutableSharedFlow<Int?>(replay = 1)


    // History buffer for step-back debugging
    private val historyBuffer = ArrayDeque<HistoryEntry>(100) // Fixed size of 100 entries

    init {
        resetMemory()
        resetProgram()
        decodeCurrentInstructionAndUpdatePointers()
    }

    fun decodeCurrentInstructionAndUpdatePointers() {
        val memory = _memory.value
        val pcp = memory[PC]

        _pcPointer.value = pcp

        val instr = memory[pcp]

        // Phase 1: Decode instruction
        if (instr == 0) {
            // HALT instruction, do nothing
            _readPointer.value = null
            _writePointer.value = null
            return
        }

        val src = instr / 100 // high two digits
        val dst = instr % 100 // low two digits

        // Phase 2: Place read pointer
        _readPointer.value = src

        // Phase 3: Place write pointer
        _writePointer.value = dst
    }

    /**
     * Executes a single step of the simulation.
     * The execution is split into phases as specified:
     * 1. Decode instruction
     * 2. Place read pointer
     * 3. Place write pointer
     * 4. Copy value
     * 5. Move PC to +1 address
     */
    fun step() {
        val currentMemory = _memory.value.copyOf()
        val pcBefore = currentMemory[1]
        val instr = currentMemory[pcBefore]

        // Phase 1: Decode instruction
        if (instr == 0) {
            // HALT instruction, do nothing
            return
        }

        val (src, dst) = decodeInstruction(instr)

        // Save history before making changes
        pushHistory(pcBefore, src, dst, currentMemory)

        // Phase 4: Copy value
        val newMemory = currentMemory.copyOf()
        val value = currentMemory[src]
        newMemory[dst] = value

        if (dst == OUT) {
            output.tryEmit(value)
        }

        // Phase 5: Move PC to +1 address
        val newPc = (newMemory[1] + 1) % 100
        newMemory[PC] = newPc

        // Update derived registers (cells 04-07)
        recomputeDerivedRegisters(newMemory)

        // Update memory
        _memory.value = newMemory

        decodeCurrentInstructionAndUpdatePointers()
    }

    /**
     * Recomputes the derived registers (cells 04-07) based on the current memory state.
     */
    private fun recomputeDerivedRegisters(memory: IntArray) {
        val a = memory[OP_A]
        val b = memory[OP_B]
        val c = memory[OP_C]

        memory[SUM] = (a + b) % 10000 // A + B
        memory[SUB] = (a - b + 10000) % 10000 // A - B (ensure positive result)
        memory[CMP] = if (a > b) 1 else 0 // A > B (1/0)
        memory[TRN] = if (c != 0) a else b // A if C else B
    }

    /**
     * Pushes an entry to the history buffer for step-back debugging.
     */
    private fun pushHistory(pc: Int, src: Int, dst: Int, memory: IntArray) {
        // If buffer is full, remove oldest entry
        if (historyBuffer.size >= 100) {
            historyBuffer.removeFirst()
        }

        historyBuffer.addLast(
            HistoryEntry(
                pcBefore = pc,
                instr = memory[pc],
                src = src,
                dst = dst,
                valueSrc = memory[src],
                valueDst = memory[dst],
                outBefore = memory[11]
            )
        )
    }

    /**
     * Steps back to the previous state using the history buffer.
     * @return true if step back was successful, false if history is empty
     */
    fun stepBack(): Boolean {
        if (historyBuffer.isEmpty()) {
            return false
        }

        val entry = historyBuffer.removeLast()
        val currentMemory = _memory.value.copyOf()

        // Restore PC
        currentMemory[PC] = entry.pcBefore

        // Restore memory values
        currentMemory[entry.dst] = entry.valueDst

        // Restore output
        currentMemory[OUT] = entry.outBefore

        // Recompute derived registers
        recomputeDerivedRegisters(currentMemory)


        _memory.value = currentMemory
        // Clear pointers
        decodeCurrentInstructionAndUpdatePointers()

        output.tryEmit(null)

        return true
    }

    fun resetMemory() {
        val newMemory = _memory.value.copyOf()

        newMemory.fill(0, 0, 50)

        // Special addresses
        newMemory[HALT] = 0 // HALT opcode
        newMemory[PC] = 50 // PC starts at address 50

        // Initialize operands
        newMemory[OP_A] = 0 // A = 42
        newMemory[OP_B] = 0 // B = 10
        newMemory[OP_C] = 0  // C = 1

        // Compute derived registers
        recomputeDerivedRegisters(newMemory)

        _memory.value = newMemory

        decodeCurrentInstructionAndUpdatePointers()

        output.tryEmit(null)
    }

    fun resetProgram() {
        val newMemory = _memory.value.copyOf()
        newMemory.fill(0, fromIndex = 50)
        // Sample program: Copy values around and then halt
        // 50: MOV A to TMP (0209) - Copy A (addr 02) to TMP (addr 09)
        // 51: MOV B to A (0302) - Copy B (addr 03) to A (addr 02)
        // 52: MOV TMP to B (0903) - Copy TMP (addr 09) to B (addr 03)
        // 53: MOV A to OUT (0211) - Copy A (addr 02) to OUT (addr 11)
        // 54: MOV B to OUT (0311) - Copy B (addr 03) to OUT (addr 11)
        // 55: MOV 00 to PC (0001) - HALT by jumping to address 00
        newMemory[50] = 209
        newMemory[51] = 302
        newMemory[52] = 903
        newMemory[53] = 211
        newMemory[54] = 311
        newMemory[55] = 1

        _memory.value = newMemory
    }

    /**
     * Resets the simulator to its initial state.
     */
    fun reset() {
        resetMemory()
        historyBuffer.clear()
    }

    /**
     * Updates a memory cell with a new value.
     * This will clear the history buffer as it's a manual edit.
     */
    fun updateMemory(address: Int, value: Int) {
        if (address in 0 until 100) {
            val newMemory = _memory.value.copyOf()
            newMemory[address] = value

            // Recompute derived registers if necessary
            if (address in 2..3 || address == 8) {
                recomputeDerivedRegisters(newMemory)
            }

            _memory.value = newMemory
            historyBuffer.clear() // Clear history on manual edit
        }
    }

    /**
     * Updates the entire memory array with new values.
     * This will clear the history buffer as it's a manual edit.
     */
    fun updateMemoryArray(newMemory: IntArray) {
        if (newMemory.size == 100) {
            // Make a copy to avoid external modifications
            val memCopy = newMemory.copyOf()

            // Recompute derived registers
            recomputeDerivedRegisters(memCopy)

            _memory.value = memCopy
            historyBuffer.clear() // Clear history on manual edit
        }
    }

    /**
     * Data class representing an entry in the history buffer.
     */
    data class HistoryEntry(
        val pcBefore: Int,
        val instr: Int,
        val src: Int,
        val dst: Int,
        val valueSrc: Int,
        val valueDst: Int,
        val outBefore: Int
    )

    companion object {
        const val PC = 1
        const val OP_A = 2
        const val OP_B = 3
        const val SUM = 4
        const val SUB = 5
        const val CMP = 6
        const val TRN = 7
        const val OP_C = 8
        const val OUT = 11
        const val INP = 12
        const val HALT = 0

    }
}
