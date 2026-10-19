package com.example.taskmanager.ui.tasks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.ui.navigation.Screen
import com.example.taskmanager.ui.tasks.components.BouncyFAB
import com.example.taskmanager.ui.tasks.components.EmptyState
import com.example.taskmanager.ui.tasks.components.TaskFilterBar
import com.example.taskmanager.ui.tasks.components.TaskItem
import com.example.taskmanager.ui.tasks.components.TaskListTopAppBar
import com.example.taskmanager.ui.tasks.components.TaskProgressCard
import com.example.taskmanager.ui.tasks.components.TaskShimmerItem
import com.example.taskmanager.ui.theme.TaskColors
import com.example.taskmanager.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val context = LocalContext.current

    // events also arrive from the editor (e.g. delete), so undo works after navigating back
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is TaskEvent.Deleted -> context.getString(R.string.task_deleted)
                is TaskEvent.StatusChanged -> when {
                    event.nextOccurrence != null ->
                        context.getString(R.string.task_completed_next, DateUtils.formatDateTime(event.nextOccurrence.dueDate))
                    event.newStatus == TaskStatus.COMPLETED -> context.getString(R.string.task_completed)
                    event.newStatus == TaskStatus.PENDING -> context.getString(R.string.task_reopened)
                    else -> context.getString(R.string.task_status_changed, event.newStatus.displayName)
                }
                is TaskEvent.ClearedCompleted -> context.getString(R.string.completed_cleared, event.tasks.size)
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo(event)
        }
    }

    val openTask: (Int) -> Unit = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) { launchSingleTop = true } }
    val addTask: () -> Unit = { navController.navigate(Screen.AddTask.route) { launchSingleTop = true } }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TaskListTopAppBar(
                isSearching = isSearching,
                searchQuery = state.filters.query,
                onSearchQueryChange = viewModel::setQuery,
                onToggleSearch = viewModel::setSearching,
                onOpenSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                onClearCompleted = viewModel::clearCompleted,
                canClearCompleted = state.stats.completed > 0,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = { BouncyFAB(navController, expanded = fabExpanded) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        when {
            state.isLoading -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(paddingValues),
                userScrollEnabled = false
            ) {
                items(6) { TaskShimmerItem() }
            }

            !state.hasAnyTasks -> EmptyState(
                isFilter = false,
                onAddTaskClick = addTask,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )

            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (!isSearching) {
                    item(key = "progress") {
                        TaskProgressCard(
                            stats = state.stats,
                            selectedStatus = state.filters.status,
                            onStatusClick = viewModel::setStatusFilter,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                item(key = "filters") {
                    TaskFilterBar(
                        filters = state.filters,
                        stats = state.stats,
                        onStatusChange = viewModel::setStatusFilter,
                        onPriorityChange = viewModel::setPriorityFilter,
                        onDateChange = viewModel::setDateFilter,
                        onSortChange = viewModel::setSortOrder,
                        onClearFilters = viewModel::clearFilters,
                    )
                }
                val taskRow: @Composable (Task, Int, Modifier) -> Unit = { task, index, modifier ->
                    TaskItem(
                        task = task,
                        index = index,
                        onClick = { openTask(task.id) },
                        onDelete = { viewModel.deleteTask(task) },
                        onToggleComplete = { viewModel.toggleCompleted(task) },
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                val sections = state.sections
                when {
                    state.tasks.isEmpty() -> item(key = "no-results") {
                        EmptyState(
                            isFilter = true,
                            onAddTaskClick = addTask,
                            onClearFilters = viewModel::clearFilters,
                        )
                    }

                    sections != null -> sectionedTasks(sections, state.collapsedSections, viewModel::toggleSection, taskRow)

                    else -> {
                        item(key = "count") {
                            Text(
                                text = stringResource(R.string.results_count, state.tasks.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                        itemsIndexed(state.tasks, key = { _, task -> task.id }) { index, task ->
                            taskRow(task, index, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.sectionedTasks(
    sections: List<Pair<TaskSection, List<Task>>>,
    collapsed: Set<TaskSection>,
    onToggle: (TaskSection) -> Unit,
    taskRow: @Composable (Task, Int, Modifier) -> Unit,
) {
    // indexes run across sections so TaskItem_<n> tags match what's on screen top to bottom
    var index = 0
    sections.forEach { (section, tasks) ->
        val isCollapsed = section in collapsed
        item(key = "section-${section.name}") {
            SectionHeader(section, tasks.size, isCollapsed, { onToggle(section) }, Modifier.animateItem())
        }
        if (!isCollapsed) {
            val start = index
            itemsIndexed(tasks, key = { _, task -> task.id }) { i, task ->
                taskRow(task, start + i, Modifier.animateItem())
            }
            index += tasks.size
        }
    }
}

@Composable
private fun SectionHeader(section: TaskSection, count: Int, collapsed: Boolean, onToggle: () -> Unit, modifier: Modifier) {
    val title = stringResource(
        when (section) {
            TaskSection.OVERDUE -> R.string.section_overdue
            TaskSection.TODAY -> R.string.section_today
            TaskSection.TOMORROW -> R.string.section_tomorrow
            TaskSection.UPCOMING -> R.string.section_upcoming
            TaskSection.COMPLETED -> R.string.section_completed
        }
    )
    val accent = when (section) {
        TaskSection.OVERDUE -> MaterialTheme.colorScheme.error
        TaskSection.COMPLETED -> TaskColors.status(TaskStatus.COMPLETED)
        else -> MaterialTheme.colorScheme.primary
    }
    val rotation by animateFloatAsState(if (collapsed) -90f else 0f, label = "chevron")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(if (collapsed) R.string.section_expand else R.string.section_collapse, title),
                onClick = onToggle
            )
            .padding(start = 8.dp, end = 4.dp, top = 10.dp, bottom = 2.dp)
            .testTag("Section_${section.name}")
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation)
        )
    }
}
