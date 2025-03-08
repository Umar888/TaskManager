package com.example.taskmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@Composable
fun CircularRevealAnimation(
    isVisible: Boolean,
    reverse: Boolean = false,
    durationMillis: Int = 500,
    backgroundColor: Color = Color.White, // Add this parameter
    content: @Composable () -> Unit
) {
    val radius = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isAnimationComplete by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible, reverse) {
        if (isVisible && !reverse) {
            // Forward animation (expand)
            coroutineScope.launch {
                radius.animateTo(
                    targetValue = 1000f, // Adjust this value for the reveal size
                    animationSpec = tween(durationMillis)
                )
                isAnimationComplete = true
            }
        } else if (reverse) {
            // Reverse animation (collapse)
            coroutineScope.launch {
                radius.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis)
                )
                isAnimationComplete = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Set the background color here
            .graphicsLayer {
                if (!isAnimationComplete) {
                    clip = true
                    shape = androidx.compose.foundation.shape.CircleShape
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    this.scaleX = radius.value / 1000f
                    this.scaleY = radius.value / 1000f
                } else {
                    clip = false // Disable clipping after animation completes
                }
            }
    ) {
        content()
    }
}