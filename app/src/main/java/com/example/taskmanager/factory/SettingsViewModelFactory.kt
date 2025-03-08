package com.example.taskmanager.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.repository.SettingsRepository
import com.example.taskmanager.viewModel.SettingsViewModel

class SettingsViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val repository = SettingsRepository(context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
