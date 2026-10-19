package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmanager.ui.navigation.AppNavHost
import com.example.taskmanager.ui.settings.SettingsViewModel
import com.example.taskmanager.ui.tasks.TaskViewModel
import com.example.taskmanager.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            // null for the few ms before DataStore loads; skip a frame rather than flash the wrong theme
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val loadedSettings = settings ?: return@setContent

            AppTheme(settings = loadedSettings) {
                AppNavHost(taskViewModel, settingsViewModel)
            }
        }
    }
}
