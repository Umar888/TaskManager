package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.factory.SettingsViewModelFactory
import com.example.taskmanager.factory.TaskViewModelFactory
import com.example.taskmanager.repository.SettingsRepository
import com.example.taskmanager.ui.layer.Screen
import com.example.taskmanager.ui.screen.AddTaskScreen
import com.example.taskmanager.ui.screen.SettingsScreen
import com.example.taskmanager.ui.screen.TaskDetailScreen
import com.example.taskmanager.ui.screen.TaskListScreen
import com.example.taskmanager.utils.AppTheme
import com.example.taskmanager.viewModel.SettingsViewModel
import com.example.taskmanager.viewModel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            // Retrieve ViewModels correctly
            val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(application))
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
            val settingsRepository = SettingsRepository(applicationContext)
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val primaryColor by settingsViewModel.primaryColor.collectAsState()

            AppTheme(
                useDarkTheme = isSystemInDarkTheme() || isDarkTheme,
                settingsRepository = settingsRepository
            ) {
                NavHost(navController = navController, startDestination = Screen.TaskList.route) {
                    composable(Screen.TaskList.route,
                        /*enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = {
                                    it
                                }
                            )
                        },
                        exitTransition = {
                            fadeOut(
                                animationSpec = tween(),
                                targetAlpha = 0.99f
                            )
                        },
                        popEnterTransition = {
                            fadeIn()
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = {
                                    it
                                }
                            )
                        }*/
                    ) {
                        TaskListScreen(navController, taskViewModel)
                    }
                    composable(Screen.AddTask.route) {
                        AddTaskScreen(navController, taskViewModel)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(navController,settingsViewModel)
                    }
                    composable(Screen.TaskDetail.route,
//                        enterTransition = {
//                            scaleIn(
//                                spring(Spring.DampingRatioLowBouncy),
//                                transformOrigin = TransformOrigin(1F, 0F)
//                            )
//                        },
//                        exitTransition = null,
//                        popEnterTransition = null,
//                        popExitTransition = {
//                            scaleOut(
//                                spring(Spring.DampingRatioLowBouncy),
//                                transformOrigin = TransformOrigin(1F, 0F)
//                            )
//                        },
                        ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                        if (taskId != null) {
                            TaskDetailScreen(navController, taskViewModel, taskId)
                        }
                    }
                }
            }
        }
    }

}
