package com.example.taskmanager.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.repository.SettingsRepository
import com.example.taskmanager.utils.md_theme_light_primary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val isDarkTheme = repository.isDarkThemeFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val primaryColor = repository.primaryColorFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, md_theme_light_primary
    )

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(enabled)
        }
    }

    fun setPrimaryColor(color: Color) {
        viewModelScope.launch {
            repository.setPrimaryColor(color)
        }
    }

    fun initializeDefaultPrimaryColor(defaultColor: Color) {
        viewModelScope.launch {
            repository.initializeDefaultPrimaryColor(defaultColor)
        }
    }
}


