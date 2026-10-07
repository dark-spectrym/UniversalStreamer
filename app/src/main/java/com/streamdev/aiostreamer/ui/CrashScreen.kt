package com.streamdev.aiostreamer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-screen error view that shows a captured stacktrace (selectable, so a tester
 * can copy it). Used by MainActivity when the app failed to start or crashed on a
 * previous run.
 */
@Composable
fun CrashScreen(
    stacktrace: String,
    onContinue: (() -> Unit)?,
    onExit: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("AIO Streamer — crash report", style = MaterialTheme.typography.titleMedium)
            Text(
                "The app hit an error. Copy this and send it back so it can be fixed.",
                style = MaterialTheme.typography.bodySmall,
            )
            SelectionContainer(modifier = Modifier.weight(1f)) {
                Text(
                    text = stacktrace,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onContinue != null) {
                    Button(onClick = onContinue) { Text("Continue to app") }
                }
                OutlinedButton(onClick = onExit) { Text("Close") }
            }
        }
    }
}
