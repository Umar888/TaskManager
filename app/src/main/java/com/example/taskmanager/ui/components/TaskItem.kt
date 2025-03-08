package com.example.taskmanager.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.taskmanager.R
import com.example.taskmanager.database.Task
import com.example.taskmanager.database.TaskStatus
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    isDragging: Boolean,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
) {

    var recentlyDeletedTask by remember { mutableStateOf<Task?>(null) }

    val delete = SwipeAction(
        onSwipe = {
            recentlyDeletedTask = task
            onDelete()
        },
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Task",
                modifier = Modifier.padding(16.dp),
                tint = Color.White
            )
        }, background = Color.Red.copy(alpha = 0.5f),
        isUndo = true
    )
    val archive = SwipeAction(
        onSwipe = {
            onComplete()
        },
        icon = {
            Icon(
                painterResource(id = R.drawable.baseline_archive_24),
                contentDescription = "archive chat",
                modifier = Modifier.padding(16.dp),

                tint = Color.White

            )
        }, background = Color(0xFF50B384).copy(alpha = 0.7f)
    )


    SwipeableActionsBox(
        swipeThreshold = 200.dp,
        startActions = listOf(archive),
        endActions = listOf(delete),
        modifier = Modifier.testTag("TaskItem_$index")
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 2.dp),
            modifier = Modifier

                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description ?: "No description",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Due By: ${
                            SimpleDateFormat(
                                "MMM dd, yyyy",
                                Locale.getDefault()
                            ).format(Date(task.dueDate))
                        }",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PriorityIcon(task.priority)
                    Text(
                        text = task.status,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (task.status) {
                            TaskStatus.PENDING.toString() -> Color.Gray
                            TaskStatus.IN_PROGRESS.toString() -> Color(ContextCompat.getColor(LocalContext.current, R.color.dark_blue))
                            TaskStatus.COMPLETED.toString() -> Color(ContextCompat.getColor(LocalContext.current, R.color.dark_green))
                            else ->  Color.Gray
                        }
                    )
                }
            }
        }
    }

}



