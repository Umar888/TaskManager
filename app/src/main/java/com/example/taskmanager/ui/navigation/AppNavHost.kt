package com.example.taskmanager.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmanager.ui.editor.TaskEditorScreen
import com.example.taskmanager.ui.settings.SettingsScreen
import com.example.taskmanager.ui.settings.SettingsViewModel
import com.example.taskmanager.ui.tasks.TaskListScreen
import com.example.taskmanager.ui.tasks.TaskViewModel

private const val TRANSITION_MS = 300

@Composable
fun AppNavHost(
    taskViewModel: TaskViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route,
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(TRANSITION_MS)) + fadeIn(tween(TRANSITION_MS)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(TRANSITION_MS)) { it / 4 } + fadeOut(tween(TRANSITION_MS)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(TRANSITION_MS)) { it / 4 } + fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(TRANSITION_MS)) + fadeOut(tween(TRANSITION_MS)) },
    ) {
        composable(Screen.TaskList.route) {
            TaskListScreen(navController, taskViewModel)
        }
        composable(Screen.AddTask.route) {
            TaskEditorScreen(navController, taskViewModel, taskId = null)
        }
        composable(
            Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            TaskEditorScreen(navController, taskViewModel, taskId = backStackEntry.arguments?.getInt("taskId"))
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController, settingsViewModel)
        }
    }
}
