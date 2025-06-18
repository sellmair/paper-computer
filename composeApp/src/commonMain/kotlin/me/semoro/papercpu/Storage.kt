package me.semoro.papercpu

/**
 * Interface for storage implementations.
 * Used to save and load the program state.
 */
interface Storage {
    /**
     * Saves the program memory to local storage.
     */
    suspend fun saveProgram(memory: ProgramData)

    /**
     * Loads the program memory from local storage.
     * @return The loaded program, or null if no saved program exists
     *
     */
    suspend fun loadProgram(): ProgramData?

    suspend fun saveProgramAs(name: String, programData: ProgramData)

    /**
     * Loads the program memory from local storage.
     * @return The loaded program, or null if no saved program with such name exists
     *
     */
    suspend fun loadProgramByName(name: String): ProgramData?


    suspend fun getSavedProgramNames(): List<String>


}

fun getStorage(): Storage {
    TODO()
}