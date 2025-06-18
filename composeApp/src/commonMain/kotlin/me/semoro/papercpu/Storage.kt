package me.semoro.papercpu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

    /**
     * @param name can contain any characters
     */
    suspend fun saveProgramAs(name: String, programData: ProgramData)

    /**
     * Loads the program memory from local storage.
     * @param name can contain any characters
     * @return The loaded program, or null if no saved program with such name exists
     *
     */
    suspend fun loadProgramByName(name: String): ProgramData?

    /**
     * Deletes a program from local storage.
     * @param name can contain any characters
     */
    suspend fun deleteProgram(name: String)

    suspend fun getSavedProgramNames(): List<String>
}

/**
 * Implementation of the Storage interface that works across all platforms.
 * Uses platform-specific storage mechanisms.
 */
expect object PlatformStorageProvider {
    suspend fun setValue(key: String, value: String?)
    suspend fun getValue(key: String): String?
}

/**
 * Base implementation of the Storage interface with common functionality.
 * Platform-specific implementations extend this class.
 */
@OptIn(ExperimentalEncodingApi::class)
object CommonStorage: Storage {
    private val defaultProgramKey = "default_program"
    private val programListKey = "program_list"

    /**
     * Serializes a ProgramData object to a string.
     */
    private fun serializeProgramData(programData: ProgramData): String {
        // Convert ShortArray to ByteArray (2 bytes per short)
        val byteArray = ByteArray(programData.packed.size * 2)
        programData.packed.forEachIndexed { index, short ->
            // Store each short as two bytes (big-endian)
            byteArray[index * 2] = (short.toInt() shr 8).toByte()
            byteArray[index * 2 + 1] = short.toByte()
        }

        // Encode ByteArray to Base64 string
        return Base64.encode(byteArray)
    }

    /**
     * Deserializes a string to a ProgramData object.
     */
    private fun deserializeProgramData(serialized: String): ProgramData {
        // Decode Base64 string to ByteArray
        val byteArray = Base64.decode(serialized)

        // Convert ByteArray to ShortArray (2 bytes per short)
        val shortArray = ShortArray(byteArray.size / 2)
        for (i in shortArray.indices) {
            // Combine two bytes into a short (big-endian)
            val high = byteArray[i * 2].toInt() and 0xFF
            val low = byteArray[i * 2 + 1].toInt() and 0xFF
            shortArray[i] = ((high shl 8) or low).toShort()
        }

        return ProgramData(shortArray)
    }

    /**
     * Encodes a key to ensure it can contain arbitrary characters.
     */
    private fun encodeKey(key: String): String {
        return Base64.encode(key.encodeToByteArray())
    }

    /**
     * Decodes a key that was encoded with encodeKey.
     */
    private fun decodeKey(encodedKey: String): String {
        return Base64.decode(encodedKey).decodeToString()
    }

    /**
     * Gets the list of saved program names.
     */
    private suspend fun getProgramList(): MutableList<String> {
        val serialized = withContext(Dispatchers.Default) {
            return@withContext PlatformStorageProvider.getValue(programListKey)
        } ?: return mutableListOf()
        return serialized.split(",")
            .filter { it.isNotEmpty() }
            .map { decodeKey(it) }
            .toMutableList()
    }

    /**
     * Saves the list of program names.
     */
    private suspend fun saveProgramList(programList: List<String>) {
        val serialized = programList.joinToString(",") { encodeKey(it) }
        withContext(Dispatchers.Default) {
            PlatformStorageProvider.setValue(programListKey, serialized)
        }
    }

    override suspend fun saveProgram(memory: ProgramData) {
        val serialized = serializeProgramData(memory)
        withContext(Dispatchers.Default) {
            PlatformStorageProvider.setValue(defaultProgramKey, serialized)
        }
    }

    override suspend fun loadProgram(): ProgramData? {
        val serialized = withContext(Dispatchers.Default) {
            return@withContext PlatformStorageProvider.getValue(defaultProgramKey)
        } ?: return null
        return deserializeProgramData(serialized)
    }

    override suspend fun saveProgramAs(name: String, programData: ProgramData) {
        val serialized = serializeProgramData(programData)
        val encodedName = encodeKey(name)
        withContext(Dispatchers.Default) {
            PlatformStorageProvider.setValue("program_$encodedName", serialized)
        }

        // Add to program list if not already there
        val programList = getProgramList()
        if (!programList.contains(name)) {
            programList.add(name)
            saveProgramList(programList)
        }
    }

    override suspend fun loadProgramByName(name: String): ProgramData? {
        val encodedName = encodeKey(name)
        val serialized = withContext(Dispatchers.Default) {
            return@withContext PlatformStorageProvider.getValue("program_$encodedName")
        } ?: return null
        return deserializeProgramData(serialized)
    }

    override suspend fun getSavedProgramNames(): List<String> {
        return getProgramList()
    }

    override suspend fun deleteProgram(name: String) {
        // Remove from program list
        val programList = getProgramList()
        if (programList.contains(name)) {
            programList.remove(name)
            saveProgramList(programList)
        }

        // Delete program data
        val encodedName = encodeKey(name)
        withContext(Dispatchers.Default) {
            PlatformStorageProvider.setValue("program_$encodedName", null)
        }
    }
}

/**
 * Returns an instance of the Storage interface.
 */
fun getStorage(): Storage {
    return CommonStorage
}
