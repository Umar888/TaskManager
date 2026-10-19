package com.example.taskmanager.ui.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.util.DateUtils
import com.example.taskmanager.ui.theme.TaskColors
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun TaskItem(
    task: Task,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completeAction = SwipeAction(
        onSwipe = onToggleComplete,
        icon = {
            Icon(
                if (task.isCompleted) Icons.Default.Refresh else Icons.Default.Check,
                contentDescription = stringResource(if (task.isCompleted) R.string.mark_pending else R.string.mark_complete),
                modifier = Modifier.padding(24.dp),
                tint = Color.White
            )
        },
        background = TaskColors.status(TaskStatus.COMPLETED)
    )
    val deleteAction = SwipeAction(
        onSwipe = onDelete,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_task),
                modifier = Modifier.padding(24.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        },
        background = MaterialTheme.colorScheme.error
    )

    val contentAlpha by animateFloatAsState(if (task.isCompleted) 0.6f else 1f, label = "completedAlpha")
    val priorityColor = TaskColors.priority(task.priorityLevel)

    SwipeableActionsBox(
        swipeThreshold = 120.dp,
        startActions = listOf(completeAction),
        endActions = listOf(deleteAction),
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            // tag the clickable card (not the swipe box) so its merged semantics include the task text
            modifier = Modifier
                .fillMaxWidth()
                .testTag("TaskItem_$index")
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // priority accent stripe
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(priorityColor.copy(alpha = if (task.isCompleted) 0.35f else 1f))
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    CompletionCheck(
                        checked = task.isCompleted,
                        onClick = onToggleComplete,
                        modifier = Modifier.testTag("TaskCheck_$index")
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f).alpha(contentAlpha)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!task.description.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DueLabel(task)
                            Pill(text = task.statusType.displayName, color = TaskColors.status(task.statusType))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Pill(
                        text = task.priorityLevel.displayName,
                        color = priorityColor,
                        modifier = Modifier.alpha(contentAlpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionCheck(checked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fill by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.primary else Color.Transparent, label = "checkFill"
    )
    val stroke = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, stroke, CircleShape)
            .clickable(role = Role.Checkbox, onClick = onClick)
    ) {
        if (checked) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.mark_pending),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DueLabel(task: Task) {
    val now = System.currentTimeMillis()
    val time = DateUtils.formatTime(task.dueDate)
    val overdue = task.isOverdue(now)
    val text = when {
        overdue -> stringResource(R.string.due_overdue, DateUtils.formatShortDate(task.dueDate))
        DateUtils.isSameDay(task.dueDate, now) -> stringResource(R.string.due_today, time)
        DateUtils.isSameDay(task.dueDate, DateUtils.startOfDay(now, 1)) -> stringResource(R.string.due_tomorrow, time)
        else -> DateUtils.formatShortDate(task.dueDate) + ", " + time
    }
    val color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = if (overdue) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}
