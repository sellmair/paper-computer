package me.semoro.papercpu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences

/**
 * Desktop (JVM) implementation of Storage using java.util.prefs.Preferences.
 */
class JVMStorage : Storage {
    private val preferences = Preferences.userRoot().node("me/semoro/papercpu")

    /**
     * Saves the program memory to Preferences.
     * Only saves the program ROM part (addresses 50-99) to keep the storage size manageable.
     */
    override suspend fun saveProgram(memory: IntArray): Unit = withContext(Dispatchers.IO) {
        // Save only the program ROM part (addresses 50-99)
        for (i in 50 until 100) {
            preferences.putInt("memory_$i", memory[i])
        }
        
        // Mark that we have saved data
        preferences.putBoolean("has_saved_program", true)
        
        // Force flush to ensure data is saved
        preferences.flush()
    }

    /**
     * Loads the program memory from Preferences.
     * Returns null if no saved program exists.
     */
    override suspend fun loadProgram(): IntArray? = withContext(Dispatchers.IO) {
        // Check if we have any saved program
        if (!preferences.getBoolean("has_saved_program", false)) {
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
            memory[i] = preferences.getInt("memory_$i", 0)
        }
        
        return@withContext memory
    }
}

/**
 * Returns the JVM-specific storage implementation.
 */
actual fun getStorage(): Storage = JVMStorage()