package me.semoro.papercpu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of Storage using NSUserDefaults.
 */
class IOSStorage : Storage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    /**
     * Saves the program memory to NSUserDefaults.
     * Only saves the program ROM part (addresses 50-99) to keep the storage size manageable.
     */
    override suspend fun saveProgram(memory: IntArray): Unit = withContext(Dispatchers.Default) {
        // Save only the program ROM part (addresses 50-99)
        for (i in 50 until 100) {
            userDefaults.setInteger(memory[i].toLong(), "memory_$i")
        }

        userDefaults.synchronize()
    }

    /**
     * Loads the program memory from NSUserDefaults.
     * Returns null if no saved program exists.
     */
    override suspend fun loadProgram(): IntArray? = withContext(Dispatchers.Default) {
        // Check if we have any saved program
        if (userDefaults.objectForKey("memory_50") == null) {
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
            memory[i] = userDefaults.integerForKey("memory_$i").toInt()
        }

        return@withContext memory
    }
}

/**
 * Returns the iOS-specific storage implementation.
 */
actual fun getStorage(): Storage = IOSStorage()
