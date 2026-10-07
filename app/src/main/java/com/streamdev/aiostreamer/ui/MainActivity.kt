package com.streamdev.aiostreamer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.streamdev.aiostreamer.app.StreamerApp
import com.streamdev.aiostreamer.ui.theme.StreamerTheme

/**
 * Compose entry point. Hosts the app navigation (age gate → site catalogue →
 * per-site listing → ExoPlayer), all wired to the v9 baseline so every backend
 * call carries the trusted-client hash transparently.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val graph = (application as StreamerApp).graph
        setContent {
            StreamerTheme {
                AppNav(graph = graph, onExit = { finish() })
            }
        }
    }
}
