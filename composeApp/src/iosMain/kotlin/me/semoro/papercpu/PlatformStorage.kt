package me.semoro.papercpu

import platform.Foundation.NSUserDefaults
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * iOS-specific implementation of the Storage interface.
 * Uses NSUserDefaults for persistent storage.
 */
actual object PlatformStorageProvider {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual suspend fun getValue(key: String): String? {
        return userDefaults.stringForKey(key)
    }

    actual suspend fun setValue(key: String, value: String?) {
        if (value != null) {
            userDefaults.setObject(value, key)
        } else {
            userDefaults.removeObjectForKey(key)
        }
    }
}