package com.example.taskmanager.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanager.database.TaskStatus
import com.example.taskmanager.ui.components.AnimatedListColumn
import com.example.taskmanager.ui.components.BouncyFAB
import com.example.taskmanager.ui.components.EmptyStateUI
import com.example.taskmanager.ui.components.TaskItem
import com.example.taskmanager.ui.components.TaskListTopAppBar
import com.example.taskmanager.ui.components.TaskProgressBar
import com.example.taskmanager.ui.components.TaskShimmerItem
import com.example.taskmanager.ui.components.filtering.DateFiltering
import com.example.taskmanager.ui.components.filtering.PriorityFiltering
import com.example.taskmanager.ui.components.filtering.SortFiltering
import com.example.taskmanager.ui.components.filtering.StatusFiltering
import com.example.taskmanager.ui.layer.Screen
import com.example.taskmanager.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = true)
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedPriority by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedDateFilter by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Latest") }

    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var filteredTasks = tasks.filter { task ->
        (selectedPriority == "All" || task.priority.lowercase() == selectedPriority.lowercase()) &&
                (selectedStatus == "All" || task.status.lowercase() == selectedStatus.lowercase()) &&
                (selectedDateFilter == "All" || filterByDate(task.dueDate, selectedDateFilter))
    }.sortedByDescending { task ->
        if (sortOrder == "Latest") -task.dueDate else task.dueDate
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        topBar = {
            TaskListTopAppBar(
                navController =navController,
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = { isSearching = !isSearching }
            )
        },
        floatingActionButton = { BouncyFAB(navController) }
    ) { paddingValues ->

        if (!isLoading && tasks.isEmpty()) {
            EmptyStateUI(onAddTaskClick = {
                coroutineScope.launch {
                    scale.animateTo(0.6f, animationSpec = tween(100)) // Shrink effect
                    scale.animateTo(1.3f, animationSpec = tween(150)) // Slight bounce up
                    scale.animateTo(1f, animationSpec = tween(100)) // Back to normal
                    navController.navigate(Screen.AddTask.route) // Navigate after animation completes
                }}, isFilter = false)
        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
            ) {

                Spacer(modifier = Modifier.height(8.dp)) // Reduced space before progress bar

                // Horizontally centered progress bar
                TaskProgressBar(completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED.toString() }.size, totalTasks = tasks.size)

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityFiltering(
                        priority = selectedPriority,
                        onPriorityChange = { selectedPriority = it },
                        modifier = Modifier.weight(1f).testTag("PriorityFiltering")
                    )
                    StatusFiltering(
                        status = selectedStatus,
                        onStatusChange = { selectedStatus = it },
                        modifier = Modifier.weight(1f).testTag("StatusFiltering")
                    )
                }


                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFiltering (date = selectedDateFilter,
                        modifier = Modifier.weight(1f).testTag("DateFiltering"),
                        onDateChanged = {
                            selectedDateFilter = it
                        })
                    SortFiltering (sortBy = sortOrder,modifier = Modifier.weight(1f).testTag("SortFiltering"),
                        onSortChange = {
                            sortOrder = it
                        })
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(10) { TaskShimmerItem() }
                    }
                } else {
                    if(filteredTasks.any { it.title.contains(searchQuery, ignoreCase = true) }){
                        AnimatedListColumn(
                            items = filteredTasks.filter { it.title.contains(searchQuery, ignoreCase = true) },
                            keySelector = { it.id.toString() },
                            onMove = { from, to ->
                                filteredTasks = filteredTasks.toMutableList().apply {
                                    add(to, removeAt(from))
                                }
                            },
                            itemContent = { index, task,  isDragging ->
                                TaskItem(
                                    task = task,
                                    isDragging = isDragging,
                                    index = index,
                                    onClick = {
                                        navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                    },
                                    onDelete = {
                                        viewModel.deleteTask(task)
                                        scope.launch {
                                            delay(1000)
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task Deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.addTask(task)
                                            }
                                        }
                                    },
                                    onComplete = {
                                        if (task.status == TaskStatus.COMPLETED.toString()) {
                                            viewModel.updateTask(
                                                task.copy(
                                                    status = TaskStatus.PENDING.toString()
                                                )
                                            )

                                            scope.launch {
                                                delay(1000)
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Task marked as Pending",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.updateTask(
                                                        task.copy(
                                                            status = TaskStatus.COMPLETED.toString()
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            viewModel.updateTask(
                                                task.copy(
                                                    status = TaskStatus.COMPLETED.toString()
                                                )
                                            )

                                            scope.launch {
                                                delay(1000)
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Task marked as Completed",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.updateTask(
                                                        task.copy(
                                                            status = TaskStatus.PENDING.toString()
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                    else{
                        EmptyStateUI(onAddTaskClick = { navController.navigate(Screen.AddTask.route) }, isFilter = true)

                    }
                }
            }
        }

    }
}
fun filterByDate(dueDate: Long, selectedDateFilter: String): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis

    return when (selectedDateFilter) {
        "Today" -> isSameDay(dueDate, today)
        "Tomorrow" -> isSameDay(dueDate, today + TimeUnit.DAYS.toMillis(1))
        "This Week" -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val weekStart = calendar.timeInMillis
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val weekEnd = calendar.timeInMillis
            dueDate in weekStart..weekEnd
        }
        "This Month" -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis
            dueDate in monthStart..monthEnd
        }
        "This Year" -> {
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val yearStart = calendar.timeInMillis
            calendar.add(Calendar.YEAR, 1)
            val yearEnd = calendar.timeInMillis
            dueDate in yearStart..yearEnd
        }
        else -> true // "All" case
    }
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}





