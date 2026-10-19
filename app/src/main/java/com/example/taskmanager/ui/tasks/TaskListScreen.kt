package com.example.taskmanager.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.ui.tasks.components.BouncyFAB
import com.example.taskmanager.ui.tasks.components.EmptyState
import com.example.taskmanager.ui.tasks.components.TaskItem
import com.example.taskmanager.ui.tasks.components.TaskListTopAppBar
import com.example.taskmanager.ui.tasks.components.TaskProgressCard
import com.example.taskmanager.ui.tasks.components.TaskShimmerItem
import com.example.taskmanager.ui.tasks.components.TaskFilterBar
import com.example.taskmanager.ui.navigation.Screen
import com.example.taskmanager.ui.tasks.TaskEvent
import com.example.taskmanager.ui.tasks.TaskViewModel

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
                is TaskEvent.StatusChanged -> when (event.newStatus) {
                    TaskStatus.COMPLETED -> context.getString(R.string.task_completed)
                    TaskStatus.PENDING -> context.getString(R.string.task_reopened)
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
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (!isSearching) {
                    item(key = "progress") {
                        TaskProgressCard(
                            stats = state.stats,
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
                    )
                }
                if (state.tasks.isEmpty()) {
                    item(key = "no-results") {
                        EmptyState(
                            isFilter = true,
                            onAddTaskClick = addTask,
                            onClearFilters = viewModel::clearFilters,
                        )
                    }
                } else {
                    item(key = "count") {
                        Text(
                            text = stringResource(R.string.results_count, state.tasks.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    itemsIndexed(state.tasks, key = { _, task -> task.id }) { index, task ->
                        TaskItem(
                            task = task,
                            index = index,
                            onClick = { openTask(task.id) },
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleComplete = { viewModel.toggleCompleted(task) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}
