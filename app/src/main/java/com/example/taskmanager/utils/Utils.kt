package com.example.taskmanager.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

object Utils {
    fun generateColorScheme(primary: Color, isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            darkColorScheme(
                primary = primary,
                onPrimary = getContrastColor(primary), // High contrast color (black or white)
                primaryContainer = lerp(primary, Color.Black, 0.4f), // Darker shade
                onPrimaryContainer = lerp(primary, Color.White, 0.8f) // Lighter contrast shade
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = getContrastColor(primary),
                primaryContainer = lerp(primary, Color.White, 0.4f), // Lighter shade
                onPrimaryContainer = lerp(primary, Color.Black, 0.8f) // Darker contrast shade
            )
        }
    }

    fun getContrastColor(color: Color): Color {
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        return if (luminance > 0.5) Color.Black else Color.White
    }
}