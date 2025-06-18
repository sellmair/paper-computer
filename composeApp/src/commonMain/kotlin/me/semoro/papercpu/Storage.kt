package me.semoro.papercpu

/**
 * Interface for platform-specific storage implementations.
 * Used to save and load the program state.
 */
interface Storage {
    /**
     * Saves the program memory to local storage.
     * @param memory The memory array to save
     */
    suspend fun saveProgram(memory: IntArray)

    /**
     * Loads the program memory from local storage.
     * @return The loaded memory array, or null if no saved program exists
     */
    suspend fun loadProgram(): IntArray?
}

/**
 * Expect function to get the platform-specific storage implementation.
 */
expect fun getStorage(): Storage