package me.semoro.papercpu

import java.util.prefs.Preferences
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Desktop (JVM) specific implementation of the Storage interface.
 * Uses java.util.prefs.Preferences for persistent storage.
 */
actual object PlatformStorageProvider {
    private val preferences = Preferences.userRoot().node("me/semoro/papercpu")
    
    actual suspend fun getValue(key: String): String? {
        val value = preferences.get(key, null)
        return value
    }
    
    actual suspend fun setValue(key: String, value: String?) {
        if (value != null) {
            preferences.put(key, value)
        } else {
            preferences.remove(key)
        }
        preferences.flush()
    }
}