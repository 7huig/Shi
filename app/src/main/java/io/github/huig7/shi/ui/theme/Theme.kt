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
 * 这一版把主色、次色与三级色整体提高饱和度：青绿主色负责行动，
 * 珊瑚色负责提醒，琥珀色负责目标与进度；纸面仍保留暖色，避免高饱和色显得刺眼。
 */
private val LightColors = lightColorScheme(
    primary = Pine,
    onPrimary = Color.White,
    primaryContainer = PineSoft,
    onPrimaryContainer = Color(0xFF003731),
    inversePrimary = Color(0xFF4DD9C5),
    secondary = Clay,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD5),
    onSecondaryContainer = Color(0xFF5A120D),
    tertiary = Color(0xFFE89A00),
    onTertiary = Color(0xFF352400),
    tertiaryContainer = Color(0xFFFFE3A3),
    onTertiaryContainer = Color(0xFF3D2A00),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3ECE0),
    onSurfaceVariant = InkMuted,
    surfaceTint = Pine,
    inverseSurface = Color(0xFF26332F),
    inverseOnSurface = Color(0xFFF0FBF7),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFFB8AD9B),
    outlineVariant = Hairline,
    scrim = Color.Black,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFECE3D4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF8EC),
    surfaceContainer = Color(0xFFFBF3E6),
    surfaceContainerHigh = Color(0xFFF5EBDC),
    surfaceContainerHighest = Color(0xFFEFE4D3),
)

private val DarkColors = darkColorScheme(
    primary = NightPine,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005046),
    onPrimaryContainer = Color(0xFFB8FFF4),
    inversePrimary = Pine,
    secondary = Color(0xFFFF8F86),
    onSecondary = Color(0xFF4A0F0A),
    secondaryContainer = Color(0xFF6E241D),
    onSecondaryContainer = Color(0xFFFFDAD5),
    tertiary = Color(0xFFFFC44D),
    onTertiary = Color(0xFF3A2800),
    tertiaryContainer = Color(0xFF5A4200),
    onTertiaryContainer = Color(0xFFFFE2A3),
    background = PaperDark,
    onBackground = NightInk,
    surface = PaperDark,
    onSurface = NightInk,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightMuted,
    surfaceTint = NightPine,
    inverseSurface = Color(0xFFE6F4EF),
    inverseOnSurface = Color(0xFF26332F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF6F857E),
    outlineVariant = NightHairline,
    scrim = Color.Black,
    surfaceBright = Color(0xFF2A3631),
    surfaceDim = Color(0xFF090F0D),
    surfaceContainerLowest = Color(0xFF080D0B),
    surfaceContainerLow = Color(0xFF131C19),
    surfaceContainer = Color(0xFF18221F),
    surfaceContainerHigh = Color(0xFF1F2B27),
    surfaceContainerHighest = Color(0xFF26332F),
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
