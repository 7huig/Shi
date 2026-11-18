package io.github.huig7.shi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 完整的 Material 3 角色表。
 *
 * 只覆盖 primary / surface 会让 FilterChip、AlertDialog、DatePicker、Snackbar
 * 落回 M3 默认的淡紫色，与纸墨色体系冲突；这里把容器类与反色类角色一并补齐。
 */
private val LightColors = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = PineSoft,
    onPrimaryContainer = Color(0xFF183B31),
    inversePrimary = Color(0xFFA8D5C2),
    secondary = Clay,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E0D7),
    onSecondaryContainer = Color(0xFF5C2F21),
    tertiary = Color(0xFF8A6A2F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4E7CC),
    onTertiaryContainer = Color(0xFF3E2E0C),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF2F0EB),
    onSurfaceVariant = InkMuted,
    surfaceTint = Pine,
    inverseSurface = Color(0xFF2E332F),
    inverseOnSurface = Color(0xFFF3F5F1),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFFB6B2A8),
    outlineVariant = Hairline,
    scrim = Color.Black,
    surfaceBright = Paper,
    surfaceDim = Color(0xFFE7E2D9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F5F0),
    surfaceContainer = Color(0xFFF4F1EB),
    surfaceContainerHigh = Color(0xFFEFEBE4),
    surfaceContainerHighest = Color(0xFFE9E4DC),
)

private val DarkColors = darkColorScheme(
    primary = NightPine,
    onPrimary = Color(0xFF17332B),
    primaryContainer = Color(0xFF2A4A3F),
    onPrimaryContainer = NightInk,
    inversePrimary = Pine,
    secondary = Color(0xFFE0A48C),
    onSecondary = Color(0xFF3F2118),
    secondaryContainer = Color(0xFF5A3A2E),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFD8C08A),
    onTertiary = Color(0xFF3A2F14),
    tertiaryContainer = Color(0xFF4A3E22),
    onTertiaryContainer = Color(0xFFF5E6BE),
    background = PaperDark,
    onBackground = NightInk,
    surface = PaperDark,
    onSurface = NightInk,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightMuted,
    surfaceTint = NightPine,
    inverseSurface = Color(0xFFE7EAE5),
    inverseOnSurface = Color(0xFF2A2E2A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF757B75),
    outlineVariant = NightHairline,
    scrim = Color.Black,
    surfaceBright = Color(0xFF2A2F29),
    surfaceDim = Color(0xFF0E100E),
    surfaceContainerLowest = Color(0xFF0B0D0B),
    surfaceContainerLow = Color(0xFF171A17),
    surfaceContainer = Color(0xFF1A1D1A),
    surfaceContainerHigh = Color(0xFF20241F),
    surfaceContainerHighest = Color(0xFF262B25),
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
