package com.example.taskmanager.ui.tasks.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun BouncyFAB(
    navController: NavController,
    scaleState: Animatable<Float, AnimationVector1D> = remember { Animatable(1f) },
    expanded: Boolean = true,
) {
    val coroutineScope = rememberCoroutineScope()

    ExtendedFloatingActionButton(
        onClick = {
            coroutineScope.launch {
                scaleState.animateTo(0.85f, animationSpec = tween(90))
                scaleState.animateTo(1.08f, animationSpec = tween(120))
                scaleState.animateTo(1f, animationSpec = tween(90))
                navController.navigate(Screen.AddTask.route) { launchSingleTop = true }
            }
        },
        expanded = expanded,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.new_task)) },
        modifier = Modifier
            .scale(scaleState.value)
            .testTag("AddTask")
    )
}
