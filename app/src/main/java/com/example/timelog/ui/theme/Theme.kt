package com.example.timelog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = PineSoft,
    onPrimaryContainer = Pine,
    secondary = Clay,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F0EB),
    onSurfaceVariant = InkMuted,
    outline = Color(0xFFB6B2A8),
    outlineVariant = Hairline,
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = NightPine,
    onPrimary = Color(0xFF17332B),
    primaryContainer = Color(0xFF2A4A3F),
    onPrimaryContainer = NightInk,
    secondary = Color(0xFFE0A48C),
    onSecondary = Color(0xFF3F2118),
    background = PaperDark,
    onBackground = NightInk,
    surface = PaperDark,
    onSurface = NightInk,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightMuted,
    outline = Color(0xFF757B75),
    outlineVariant = NightHairline,
    error = Color(0xFFFFB4AB),
)

@Composable
fun TimeLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TimeLogTypography,
        content = content,
    )
}
