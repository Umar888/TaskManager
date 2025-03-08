package com.example.taskmanager.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskProgressBar(
    completedTasks: Int,
    totalTasks: Int
) {
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .padding(16.dp)
    ) {
        // Background Circle
        Canvas(modifier = Modifier.size(160.dp)) {
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = size.minDimension / 2,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Animated Circular Progress
        CircularProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier.size(160.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 12.dp
        )

        // Text in the center
        Text(
            text = "${(animatedProgress.value * 100).toInt()}%",
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

