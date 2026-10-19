package com.example.taskmanager.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.local.TaskDatabase
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.data.repository.RoomTaskRepository
import com.example.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskListUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val stats: TaskStats = TaskStats(),
    val filters: TaskFilters = TaskFilters(),
) {
    val hasAnyTasks: Boolean get() = stats.total > 0
}

/** One-off events the list screen turns into snackbars with an Undo action. */
sealed interface TaskEvent {
    data class Deleted(val task: Task) : TaskEvent
    data class StatusChanged(val previous: Task, val newStatus: TaskStatus) : TaskEvent
    data class ClearedCompleted(val tasks: List<Task>) : TaskEvent
}

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val filters = MutableStateFlow(TaskFilters())

    val uiState: StateFlow<TaskListUiState> =
        combine(repository.allTasks, filters) { tasks, filters ->
            val now = System.currentTimeMillis()
            TaskListUiState(
                isLoading = false,
                tasks = filters.apply(tasks, now),
                stats = TaskStats.from(tasks, now),
                filters = filters,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    private val _events = Channel<TaskEvent>(Channel.BUFFERED)
    val events: Flow<TaskEvent> = _events.receiveAsFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun observeTask(id: Int): Flow<Task?> = repository.observeTask(id)

    fun setSearching(searching: Boolean) {
        _isSearching.value = searching
        if (!searching) filters.update { it.copy(query = "") }
    }

    fun setQuery(query: String) = filters.update { it.copy(query = query) }
    fun setStatusFilter(status: TaskStatus?) = filters.update { it.copy(status = status) }
    fun setPriorityFilter(priority: TaskPriority?) = filters.update { it.copy(priority = priority) }
    fun setDateFilter(date: DateFilter) = filters.update { it.copy(date = date) }
    fun setSortOrder(sort: SortOrder) = filters.update { it.copy(sort = sort) }
    fun clearFilters() = filters.update { TaskFilters(sort = it.sort) }

    fun saveTask(task: Task) {
        viewModelScope.launch { repository.save(task.copy(title = task.title.trim(), description = task.description?.trim())) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            _events.send(TaskEvent.Deleted(task))
        }
    }

    fun toggleCompleted(task: Task) {
        val newStatus = if (task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
        viewModelScope.launch {
            repository.save(task.copy(status = newStatus.toString()))
            _events.send(TaskEvent.StatusChanged(task, newStatus))
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            // read from the repo, not uiState, so undo restores completed tasks hidden by filters too
            val completed = repository.allTasks.first().filter { it.isCompleted }
            if (completed.isEmpty()) return@launch
            repository.deleteCompleted()
            _events.send(TaskEvent.ClearedCompleted(completed))
        }
    }

    fun undo(event: TaskEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskEvent.Deleted -> repository.save(event.task)
                is TaskEvent.StatusChanged -> repository.save(event.previous)
                is TaskEvent.ClearedCompleted -> event.tasks.forEach { repository.save(it) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                TaskViewModel(RoomTaskRepository(TaskDatabase.getDatabase(app).taskDao()))
            }
        }
    }
}
