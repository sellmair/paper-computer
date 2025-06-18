package me.semoro.papercpu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.browser.localStorage

/**
 * Web (WASM) implementation of Storage using localStorage.
 */
class WasmStorage : Storage {
    /**
     * Saves the program memory to localStorage.
     * Only saves the program ROM part (addresses 50-99) to keep the storage size manageable.
     */
    override suspend fun saveProgram(memory: IntArray): Unit = withContext(Dispatchers.Default) {
        // Save only the program ROM part (addresses 50-99)
        for (i in 50 until 100) {
            localStorage.setItem("memory_$i", memory[i].toString())
        }
        
        // Mark that we have saved data
        localStorage.setItem("has_saved_program", "true")
    }

    /**
     * Loads the program memory from localStorage.
     * Returns null if no saved program exists.
     */
    override suspend fun loadProgram(): IntArray? = withContext(Dispatchers.Default) {
        // Check if we have any saved program
        if (localStorage.getItem("has_saved_program") != "true") {
            return@withContext null
        }
        
        // Create a new memory array with default values
        val memory = IntArray(100) { 0 }
        
        // Special addresses
        memory[0] = 0 // HALT opcode
        memory[1] = 50 // PC starts at address 50
        
        // Initialize operands
        memory[2] = 42 // A = 42
        memory[3] = 10 // B = 10
        memory[8] = 1  // C = 1
        
        // Load the program ROM part (addresses 50-99)
        for (i in 50 until 100) {
            val value = localStorage.getItem("memory_$i")
            if (value != null) {
                memory[i] = value.toIntOrNull() ?: 0
            }
        }
        
        return@withContext memory
    }
}

/**
 * Returns the Web-specific storage implementation.
 */
actual fun getStorage(): Storage = WasmStorage()