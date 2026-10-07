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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * One-time 18+ age gate (persisted via the `oldenough` preference, matching the
 * original NavDrawer1 gate). Declining exits the app.
 */
@Composable
fun AgeGateScreen(onConfirm: () -> Unit, onDecline: () -> Unit) {
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
        Button(onClick = onConfirm, modifier = Modifier.padding(bottom = 12.dp)) {
            Text("I am 18 or older — Enter")
        }
        OutlinedButton(onClick = onDecline) { Text("Exit") }
    }
}
