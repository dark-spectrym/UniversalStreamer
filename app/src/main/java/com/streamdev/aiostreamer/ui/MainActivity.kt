package com.streamdev.aiostreamer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.streamdev.aiostreamer.app.StreamerApp
import com.streamdev.aiostreamer.core.CrashLog
import com.streamdev.aiostreamer.ui.theme.StreamerTheme

/**
 * Compose entry point. Hosts the app navigation (age gate → site catalogue →
 * per-site listing → ExoPlayer). If startup failed or a previous run crashed, the
 * captured stacktrace is shown on screen instead (diagnosable without adb).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as StreamerApp
        val savedCrash = CrashLog.read(this)
        val startupError = app.startupError?.stackTraceToString()
        val report = startupError ?: savedCrash
        val graphOk = app.graph != null && app.startupError == null

        setContent {
            StreamerTheme {
                var dismissed by remember { mutableStateOf(false) }
                val graph = app.graph
                when {
                    report != null && !dismissed -> CrashScreen(
                        stacktrace = report,
                        onContinue = if (graphOk) {
                            { CrashLog.clear(this); dismissed = true }
                        } else null,
                        onExit = { finish() },
                    )
                    graph != null -> AppNav(graph = graph, onExit = { finish() })
                    else -> CrashScreen(
                        stacktrace = "Startup failed and no details were captured.",
                        onContinue = null,
                        onExit = { finish() },
                    )
                }
            }
        }
    }
}
