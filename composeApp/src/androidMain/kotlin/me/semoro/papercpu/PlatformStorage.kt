package me.semoro.papercpu

import android.content.Context
import android.content.SharedPreferences
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.core.content.edit

/**
 * Holder for the Android-specific storage implementation.
 */
actual object PlatformStorageProvider {
    var storage: AndroidStorage? = null

    actual suspend fun setValue(key: String, value: String?) {
        if (value != null) {
            storage!!.set(key, value)
        } else {
            storage!!.remove(key)
        }
    }

    actual suspend fun getValue(key: String): String? {
        return storage!!.get(key)
    }
}

/**
 * Android-specific implementation of the Storage interface.
 * Uses SharedPreferences for persistent storage.
 */
@OptIn(ExperimentalEncodingApi::class)
class AndroidStorage(private val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("PaperCPU_Storage", Context.MODE_PRIVATE)
    }

    fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun set(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }

    fun remove(key: String) {
        sharedPreferences.edit {
            remove(key)
        }
    }
}
