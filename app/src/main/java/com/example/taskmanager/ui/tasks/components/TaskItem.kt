package com.example.taskmanager.ui.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R
import com.example.taskmanager.data.model.RepeatRule
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
    val haptics = LocalHapticFeedback.current
    val toggle = {
        haptics.performHapticFeedback(if (task.isCompleted) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
        onToggleComplete()
    }
    val doneColor = TaskColors.status(TaskStatus.COMPLETED)
    val completeAction = SwipeAction(
        onSwipe = toggle,
        icon = {
            Icon(
                if (task.isCompleted) Icons.Default.Refresh else Icons.Default.Check,
                contentDescription = stringResource(if (task.isCompleted) R.string.mark_pending else R.string.mark_complete),
                modifier = Modifier.padding(24.dp),
                tint = MaterialTheme.colorScheme.surface
            )
        },
        background = doneColor
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

    val contentAlpha by animateFloatAsState(if (task.isCompleted) 0.55f else 1f, label = "completedAlpha")
    val priorityColor = TaskColors.priority(task.priorityLevel)
    val shape = MaterialTheme.shapes.large

    SwipeableActionsBox(
        swipeThreshold = 120.dp,
        startActions = listOf(completeAction),
        endActions = listOf(deleteAction),
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.clip(shape)
    ) {
        Card(
            onClick = onClick,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            // tag the clickable card (not the swipe box) so its merged semantics include the task text
            modifier = Modifier
                .fillMaxWidth()
                .testTag("TaskItem_$index")
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                CompletionCheck(
                    checked = task.isCompleted,
                    ringColor = priorityColor,
                    onClick = toggle,
                    modifier = Modifier.testTag("TaskCheck_$index")
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f).alpha(contentAlpha)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        PriorityFlag(task.priorityLevel.displayName, priorityColor)
                    }
                    if (!task.description.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    MetaRow(task)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetaRow(task: Task) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        DueLabel(task)
        if (task.statusType == TaskStatus.IN_PROGRESS) {
            Pill(text = task.statusType.displayName, color = TaskColors.status(task.statusType))
        }
        val items = task.checklistItems
        if (items.isNotEmpty()) {
            MetaIcon(
                painter = painterResource(R.drawable.ic_checklist),
                text = stringResource(R.string.checklist_progress, items.count { it.done }, items.size),
                tint = if (items.all { it.done }) TaskColors.status(TaskStatus.COMPLETED) else muted
            )
        }
        if (task.reminderMinutes != null && !task.isCompleted) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = stringResource(R.string.has_reminder),
                tint = muted,
                modifier = Modifier.size(16.dp)
            )
        }
        if (task.repeatRule != RepeatRule.NONE) {
            MetaIcon(
                painter = painterResource(R.drawable.ic_repeat),
                text = task.repeatRule.displayName,
                tint = muted,
                contentDescription = stringResource(R.string.repeats, task.repeatRule.displayName)
            )
        }
    }
}

@Composable
private fun MetaIcon(painter: Painter, text: String, tint: Color, contentDescription: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
private fun PriorityFlag(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(start = 6.dp, end = 9.dp, top = 3.dp, bottom = 3.dp)
    ) {
        Icon(painterResource(R.drawable.ic_flag), contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
private fun CompletionCheck(checked: Boolean, ringColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fill by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.primary else Color.Transparent, label = "checkFill"
    )
    val scale by animateFloatAsState(
        if (checked) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale"
    )
    val stroke = if (checked) MaterialTheme.colorScheme.primary else ringColor
    val label = stringResource(if (checked) R.string.mark_pending else R.string.mark_complete)
    // 40dp touch target around a 26dp circle
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = label }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, stroke, CircleShape)
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
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
        else -> DateUtils.formatDateTime(task.dueDate)
    }
    val color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_schedule), contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
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
