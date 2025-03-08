package com.example.taskmanager.repository
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanager.utils.Utils.generateColorScheme
import com.example.taskmanager.utils.md_theme_dark_onPrimary
import com.example.taskmanager.utils.md_theme_dark_onPrimaryContainer
import com.example.taskmanager.utils.md_theme_dark_primaryContainer
import com.example.taskmanager.utils.md_theme_light_onPrimary
import com.example.taskmanager.utils.md_theme_light_onPrimaryContainer
import com.example.taskmanager.utils.md_theme_light_primary
import com.example.taskmanager.utils.md_theme_light_primaryContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Extension property for DataStore instance
val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore

    val isDarkThemeFlow: Flow<Boolean> = dataStore.data
        .map { it[PreferencesKeys.DARK_THEME] ?: false }

    val primaryColorFlow: Flow<ColorScheme> = dataStore.data
        .map { preferences ->
            val storedColor = preferences[PreferencesKeys.PRIMARY_COLOR]
            val primaryColor = if (storedColor != null) Color(storedColor) else md_theme_light_primary

            generateColorScheme(primaryColor, isDarkThemeFlow.first())
        }




    suspend fun setPrimaryColor(color: Color) {
        dataStore.edit { it[PreferencesKeys.PRIMARY_COLOR] = color.toArgb() }
    }

    private fun createColorScheme(primary: Color, isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            darkColorScheme(
                primary = primary,
                onPrimary = md_theme_dark_onPrimary,
                primaryContainer = md_theme_dark_primaryContainer,
                onPrimaryContainer = md_theme_dark_onPrimaryContainer
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = md_theme_light_onPrimary,
                primaryContainer = md_theme_light_primaryContainer,
                onPrimaryContainer = md_theme_light_onPrimaryContainer
            )
        }
    }


    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun initializeDefaultPrimaryColor(defaultColor: Color) {
        val preferences = dataStore.data.firstOrNull()
        if (preferences?.get(PreferencesKeys.PRIMARY_COLOR) == null) {
            setPrimaryColor(defaultColor) // Use the default theme color
        }
    }

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val PRIMARY_COLOR = intPreferencesKey("primary_color")
    }
}

