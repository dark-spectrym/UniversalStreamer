package com.streamdev.aiostreamer.ui.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.streamdev.aiostreamer.BuildConfig

/**
 * Entry / 18+ screen. Also the guaranteed proof-of-life first frame: it has no
 * dependencies (no network, no prefs needed to render) and requests focus on its
 * primary button so it is operable by a TV remote (D-pad), not just touch.
 */
@Composable
fun AgeGateScreen(onConfirm: () -> Unit, onDecline: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("AIO Streamer", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This app contains adult content intended for viewers 18 years or older.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
        Button(
            onClick = onConfirm,
            modifier = Modifier.focusRequester(focus).padding(bottom = 12.dp),
        ) {
            Text("I am 18 or older — Enter")
        }
        OutlinedButton(onClick = onDecline) { Text("Exit") }
        Text(
            "build ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}
