package com.streamdev.aiostreamer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFFE53170)
private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Color(0xFFB0356A),
    background = Color(0xFF101014),
    surface = Color(0xFF17171C),
)
private val LightColors = lightColorScheme(
    primary = Accent,
    secondary = Color(0xFFB0356A),
)

/** App Material3 theme. Defaults to dark (the original app shipped a dark theme). */
@Composable
fun StreamerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
