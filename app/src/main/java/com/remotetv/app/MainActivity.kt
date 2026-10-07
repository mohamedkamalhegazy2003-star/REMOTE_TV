package com.remotetv.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

class MainActivity : ComponentActivity() {

    private val controller get() = (application as RemoteApp).controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = C.Accent, background = C.Bg, surface = C.Panel,
                    onSurface = C.Text, onBackground = C.Text
                )
            ) { RemoteScreen(controller) }
        }
    }

    override fun onStart() {
        super.onStart()
        controller.autoConnect()
    }

    // The phone's hardware volume buttons control the TV volume.
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> { controller.key(Key.VOL_UP); true }
        KeyEvent.KEYCODE_VOLUME_DOWN -> { controller.key(Key.VOL_DOWN); true }
        else -> super.onKeyDown(keyCode, event)
    }
}
