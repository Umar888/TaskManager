package com.example.taskmanager.ui.tasks.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskmanager.R
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.ui.tasks.DateFilter
import com.example.taskmanager.ui.tasks.SortOrder
import com.example.taskmanager.ui.tasks.TaskFilters
import com.example.taskmanager.ui.tasks.TaskStats

@Composable
fun TaskFilterBar(
    filters: TaskFilters,
    stats: TaskStats,
    onStatusChange: (TaskStatus?) -> Unit,
    onPriorityChange: (TaskPriority?) -> Unit,
    onDateChange: (DateFilter) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // status as one-tap chips since it's the filter people reach for most
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            StatusChip(stringResource(R.string.filter_all), stats.total, filters.status == null, "All") { onStatusChange(null) }
            StatusChip(TaskStatus.PENDING.displayName, stats.pending, filters.status == TaskStatus.PENDING, "Pending") {
                onStatusChange(TaskStatus.PENDING)
            }
            StatusChip(TaskStatus.IN_PROGRESS.displayName, stats.inProgress, filters.status == TaskStatus.IN_PROGRESS, "In Progress") {
                onStatusChange(TaskStatus.IN_PROGRESS)
            }
            StatusChip(TaskStatus.COMPLETED.displayName, stats.completed, filters.status == TaskStatus.COMPLETED, "Completed") {
                onStatusChange(TaskStatus.COMPLETED)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            MenuChip(
                label = filters.priority?.let { stringResource(R.string.priority_label, it.displayName) }
                    ?: stringResource(R.string.filter_any_priority),
                selected = filters.priority != null,
                tag = "PriorityFiltering",
                options = listOf<TaskPriority?>(null) + TaskPriority.entries.reversed(),
                optionLabel = { it?.displayName ?: stringResource(R.string.filter_all) },
                optionTag = { "PriorityOption_${it?.displayName ?: "All"}" },
                isSelected = { it == filters.priority },
                onSelect = onPriorityChange
            )
            MenuChip(
                label = filters.date.displayName,
                selected = filters.date != DateFilter.ALL,
                tag = "DateFiltering",
                options = DateFilter.entries,
                optionLabel = { it.displayName },
                optionTag = { "DateOption_${it.name}" },
                isSelected = { it == filters.date },
                onSelect = onDateChange
            )
            MenuChip(
                label = filters.sort.displayName,
                selected = false,
                tag = "SortFiltering",
                options = SortOrder.entries,
                optionLabel = { it.displayName },
                optionTag = { "SortOption_${it.name}" },
                isSelected = { it == filters.sort },
                onSelect = onSortChange
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, selected: Boolean, tagName: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$label  $count") },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null,
        modifier = Modifier.testTag("StatusOption_$tagName")
    )
}

@Composable
private fun <T> MenuChip(
    label: String,
    selected: Boolean,
    tag: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    optionTag: (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.testTag(tag)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    trailingIcon = if (isSelected(option)) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    modifier = Modifier.testTag(optionTag(option)),
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
