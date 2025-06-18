package me.semoro.papercpu

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of Storage using SharedPreferences.
 */
class AndroidStorage(private val context: Context) : Storage {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("PaperCPU", Context.MODE_PRIVATE)
    }

    /**
     * Saves the program memory to SharedPreferences.
     * Only saves the program ROM part (addresses 50-99) to keep the storage size manageable.
     */
    override suspend fun saveProgram(memory: IntArray) = withContext(Dispatchers.IO) {
        val editor = sharedPreferences.edit()
        
        // Save only the program ROM part (addresses 50-99)
        for (i in 50 until 100) {
            editor.putInt("memory_$i", memory[i])
        }
        
        editor.apply()
    }

    /**
     * Loads the program memory from SharedPreferences.
     * Returns null if no saved program exists.
     */
    override suspend fun loadProgram(): IntArray? = withContext(Dispatchers.IO) {
        // Check if we have any saved program
        if (!sharedPreferences.contains("memory_50")) {
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
            memory[i] = sharedPreferences.getInt("memory_$i", 0)
        }
        
        return@withContext memory
    }
}

/**
 * Returns the Android-specific storage implementation.
 */
actual fun getStorage(): Storage {
    // This will be initialized in MainActivity.kt
    return AndroidStorageHolder.storage
}

/**
 * Holder for the Android storage instance.
 * This is initialized in MainActivity.kt.
 */
object AndroidStorageHolder {
    lateinit var storage: Storage
}