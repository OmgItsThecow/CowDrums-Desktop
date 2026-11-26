package com.cowdrums.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Minimalist dark studio palette inspired by SF Drum Machine:
 * Matte charcoal surfaces with a single vivid orange accent (#EF6C00).
 */
object CowColors {
    val BgDark = Color(0xFF212529)
    val SurfacePanel = Color(0xFF212529)
    val SurfaceCard = Color(0xFF1B1E22)
    val SurfaceElevated = Color(0xFF2B3036)
    val BorderSubtle = Color(0xFF343A40)

    val TextPrimary = Color(0xFFF1F3F5)
    val TextSecondary = Color(0xFFA0A8B0)
    val TextMuted = Color(0xFF6C757D)

    val AccentOrange = Color(0xFFEF6C00)
    val AccentOrangeDim = Color(0xFFBF5600)
    val AccentRed = Color(0xFFE53935)

    // Backwards-compatible aliases mapped to the clean monochrome + orange system
    val AccentAmber = AccentOrange
    val AccentCoral = AccentRed
    val AccentCyan = AccentOrange
    val AccentGreen = AccentOrange
    val AccentViolet = AccentOrange
    val AccentPink = AccentOrange
}

private val CowDarkColorScheme = darkColorScheme(
    primary = CowColors.AccentOrange,
    onPrimary = Color.White,
    secondary = CowColors.AccentOrange,
    onSecondary = Color.White,
    tertiary = CowColors.AccentOrange,
    background = CowColors.BgDark,
    onBackground = CowColors.TextPrimary,
    surface = CowColors.SurfacePanel,
    onSurface = CowColors.TextPrimary,
    surfaceVariant = CowColors.SurfaceCard,
    onSurfaceVariant = CowColors.TextSecondary,
    outline = CowColors.BorderSubtle
)

@Composable
fun CowDrumsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CowDarkColorScheme,
        content = content
    )
}
