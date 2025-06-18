package me.semoro.papercpu

import kotlinx.browser.window

/**
 * Web (Wasm) specific implementation of the Storage interface.
 * Uses the browser's localStorage API for persistent storage.
 */
actual object PlatformStorageProvider {
    actual suspend fun getValue(key: String): String? {
        return window.localStorage.getItem(key)
    }

    actual suspend fun setValue(key: String, value: String?) {
        if (value != null) {
            window.localStorage.setItem(key, value)
        } else {
            window.localStorage.removeItem(key)
        }
    }

}
