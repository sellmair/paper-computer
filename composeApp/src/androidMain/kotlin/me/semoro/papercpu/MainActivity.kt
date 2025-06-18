package me.semoro.papercpu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize the storage
        PlatformStorageProvider.storage = AndroidStorage(applicationContext)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlatformStorageProvider.storage = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
