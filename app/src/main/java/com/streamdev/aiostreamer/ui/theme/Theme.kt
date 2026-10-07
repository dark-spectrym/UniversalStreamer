package com.streamdev.aiostreamer.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFFE53170)
private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Color(0xFFB0356A),
    background = Color(0xFF101014),
    surface = Color(0xFF17171C),
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC),
)

/**
 * App Material3 theme. Always dark (the original app shipped dark), and wraps
 * content in a full-size [Surface] so the window is never a bare white rectangle
 * while content is loading or if a frame is slow — important on Android TV where a
 * blank white window reads as "broken".
 */
@Composable
fun StreamerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkColors.background) {
            content()
        }
    }
}
