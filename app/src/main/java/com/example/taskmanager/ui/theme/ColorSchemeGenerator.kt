package com.example.taskmanager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

object ColorSchemeGenerator {
    /**
     * Builds on the full base scheme and only swaps the primary family, so surfaces, outlines
     * and error colours stay consistent whatever seed the user picks.
     */
    fun generateColorScheme(seed: Color, base: ColorScheme, isDarkTheme: Boolean): ColorScheme {
        // very dark seeds (maroon, indigo) disappear on a dark background, so lift them
        val primary = if (isDarkTheme) lerp(seed, Color.White, 0.35f) else seed
        val primaryContainer = if (isDarkTheme) lerp(seed, Color.Black, 0.55f) else lerp(seed, Color.White, 0.80f)
        val onPrimaryContainer = if (isDarkTheme) lerp(seed, Color.White, 0.85f) else lerp(seed, Color.Black, 0.70f)
        // neutral surfaces with a faint tint of the seed, instead of the base scheme's amber ones
        fun tone(neutral: Long, amount: Float) = lerp(Color(neutral), seed, amount)
        val surface = if (isDarkTheme) tone(0xFF121316, 0.04f) else tone(0xFFFCFCFD, 0.02f)
        return base.copy(
            background = surface,
            surface = surface,
            surfaceBright = if (isDarkTheme) tone(0xFF37383B, 0.05f) else surface,
            surfaceDim = if (isDarkTheme) surface else tone(0xFFDCDCDE, 0.05f),
            surfaceContainerLowest = if (isDarkTheme) tone(0xFF0D0E10, 0.03f) else Color.White,
            surfaceContainerLow = if (isDarkTheme) tone(0xFF1A1B1E, 0.05f) else tone(0xFFF6F6F8, 0.04f),
            surfaceContainer = if (isDarkTheme) tone(0xFF1F2023, 0.06f) else tone(0xFFF0F0F3, 0.05f),
            surfaceContainerHigh = if (isDarkTheme) tone(0xFF292A2D, 0.07f) else tone(0xFFEAEAED, 0.06f),
            surfaceContainerHighest = if (isDarkTheme) tone(0xFF343538, 0.08f) else tone(0xFFE4E4E7, 0.07f),
            surfaceVariant = if (isDarkTheme) tone(0xFF44464B, 0.08f) else tone(0xFFE2E2E7, 0.10f),
            onSurfaceVariant = if (isDarkTheme) Color(0xFFC6C6CB) else Color(0xFF46474C),
            outline = if (isDarkTheme) Color(0xFF8F9095) else Color(0xFF76777C),
            outlineVariant = if (isDarkTheme) Color(0xFF44464B) else Color(0xFFC6C6CB),
            onSurface = if (isDarkTheme) Color(0xFFE3E3E6) else Color(0xFF1B1C1F),
            onBackground = if (isDarkTheme) Color(0xFFE3E3E6) else Color(0xFF1B1C1F),
            primary = primary,
            onPrimary = getContrastColor(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = lerp(primaryContainer, base.surface, 0.4f),
            onSecondaryContainer = onPrimaryContainer,
            inversePrimary = if (isDarkTheme) seed else lerp(seed, Color.White, 0.5f),
            surfaceTint = primary,
        )
    }

    fun getContrastColor(color: Color): Color = if (color.luminance() > 0.45f) Color.Black else Color.White
}
