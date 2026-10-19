package com.example.taskmanager.ui.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.ui.tasks.TaskStats
import com.example.taskmanager.util.DateUtils

/**
 * Hero card at the top of the list: today's date, completion ring and a per-status breakdown.
 * The status tiles double as shortcuts for the status filter.
 */
@Composable
fun TaskProgressCard(
    stats: TaskStats,
    selectedStatus: TaskStatus?,
    onStatusClick: (TaskStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = stats.progress,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val colors = MaterialTheme.colorScheme
    // containers + their on-colours keep contrast right in light, dark and wallpaper themes alike
    val gradient = Brush.linearGradient(listOf(colors.primaryContainer, colors.tertiaryContainer))
    val content = colors.onPrimaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(gradient)
            .padding(20.dp)
            .testTag("ProgressCard")
    ) {
        Text(
            text = DateUtils.formatLongDay(System.currentTimeMillis()).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = content.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
                Canvas(Modifier.size(88.dp)) {
                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(content.copy(alpha = 0.12f), 0f, 360f, false, style = stroke)
                    drawArc(colors.primary, -90f, 360f * progress, false, style = stroke)
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = if (stats.total > 0 && stats.completed == stats.total) {
                        stringResource(R.string.progress_all_done)
                    } else {
                        stringResource(R.string.progress_done, stats.completed, stats.total)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        stats.overdue > 0 -> Badge(
                            stringResource(R.string.overdue_count, stats.overdue),
                            colors.error,
                            colors.onError
                        )
                        stats.dueToday == 0 -> Text(
                            stringResource(R.string.progress_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = content.copy(alpha = 0.75f)
                        )
                    }
                    if (stats.dueToday > 0) {
                        Badge(stringResource(R.string.due_today_count, stats.dueToday), colors.primary, colors.onPrimary)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                stringResource(R.string.stat_pending), stats.pending, TaskStatus.PENDING, selectedStatus, onStatusClick,
                Modifier.weight(1f)
            )
            StatTile(
                stringResource(R.string.stat_in_progress), stats.inProgress, TaskStatus.IN_PROGRESS, selectedStatus, onStatusClick,
                Modifier.weight(1f)
            )
            StatTile(
                stringResource(R.string.stat_done), stats.completed, TaskStatus.COMPLETED, selectedStatus, onStatusClick,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Badge(text: String, container: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun StatTile(
    label: String,
    value: Int,
    status: TaskStatus,
    selectedStatus: TaskStatus?,
    onClick: (TaskStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = status == selectedStatus
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) colors.primary else colors.surface.copy(alpha = 0.55f), label = "tileContainer"
    )
    val content by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurface, label = "tileContent"
    )
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            // tapping the active tile again clears the filter
            .clickable(role = Role.Tab) { onClick(if (selected) null else status) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("StatTile_${status.name}")
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = content
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content.copy(alpha = 0.75f),
            maxLines = 1
        )
    }
}
