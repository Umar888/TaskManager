package com.example.taskmanager.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanager.ui.theme.md_theme_light_primary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings_prefs")

enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val seedColor: Color = md_theme_light_primary,
    val dynamicColor: Boolean = false,
)

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val mode = prefs[Keys.THEME_MODE]?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
            // older versions only stored an on/off dark flag
            ?: if (prefs[Keys.LEGACY_DARK_THEME] == true) ThemeMode.DARK else ThemeMode.SYSTEM
        AppSettings(
            themeMode = mode,
            seedColor = prefs[Keys.PRIMARY_COLOR]?.let { Color(it) } ?: md_theme_light_primary,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSeedColor(color: Color) {
        dataStore.edit {
            it[Keys.PRIMARY_COLOR] = color.toArgb()
            // picking a colour only makes sense with wallpaper colours off
            it[Keys.DYNAMIC_COLOR] = false
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    private object Keys {
        val LEGACY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRIMARY_COLOR = intPreferencesKey("primary_color")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
}
