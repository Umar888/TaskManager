package com.example.taskmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import com.example.taskmanager.ui.layer.Screen
import kotlinx.coroutines.launch

@Composable
fun BouncyFAB(
    navController: NavController,
    scaleState: Animatable<Float, AnimationVector1D> = remember { Animatable(1f) } // Default for normal use
) {
    val coroutineScope = rememberCoroutineScope()

    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                scaleState.animateTo(0.6f, animationSpec = tween(100)) // Shrink effect
                scaleState.animateTo(1.3f, animationSpec = tween(150)) // Slight bounce up
                scaleState.animateTo(1f, animationSpec = tween(100)) // Back to normal
                navController.navigate(Screen.AddTask.route) // Navigate after animation completes
            }
        },
        modifier = Modifier.scale(scaleState.value).testTag("AddTask")
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Task")
    }
}

