package com.example.taskmanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R

@Composable
fun PriorityIcon(priority: String) {
    val icon = when (priority.lowercase()) {
        "high" -> R.drawable.high_priority
        "medium" -> R.drawable.medium_priority
        "low" -> R.drawable.low_prioriry
        else -> R.drawable.low_prioriry
    }

    Image(
        painter = painterResource(id = icon),
        contentDescription = "Priority: $priority",
        modifier = Modifier.size(24.dp),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
}
