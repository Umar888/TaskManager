package com.example.taskmanager.ui.tasks.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R
import com.example.taskmanager.ui.tasks.TaskStats

/** Branded hero card at the top of the list: completion ring plus a per-status breakdown. */
@Composable
fun TaskProgressCard(stats: TaskStats, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = stats.progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val gradient = Brush.linearGradient(listOf(primary, lerp(primary, Color.Black, 0.25f)))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(20.dp)
            .testTag("ProgressCard")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                Canvas(Modifier.size(84.dp)) {
                    val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(onPrimary.copy(alpha = 0.22f), 0f, 360f, false, style = stroke)
                    drawArc(onPrimary, -90f, 360f * progress, false, style = stroke)
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onPrimary
                )
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = stringResource(R.string.progress_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = onPrimary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (stats.total > 0 && stats.completed == stats.total) {
                        stringResource(R.string.progress_all_done)
                    } else {
                        stringResource(R.string.progress_done, stats.completed, stats.total)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onPrimary
                )
                if (stats.overdue > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.overdue_count, stats.overdue),
                        style = MaterialTheme.typography.labelMedium,
                        color = primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(onPrimary)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(stringResource(R.string.stat_pending), stats.pending, onPrimary, Modifier.weight(1f))
            StatTile(stringResource(R.string.stat_in_progress), stats.inProgress, onPrimary, Modifier.weight(1f))
            StatTile(stringResource(R.string.stat_done), stats.completed, onPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int, contentColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(contentColor.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}
