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
import com.example.taskmanager.reminder.ReminderScheduler
import com.example.taskmanager.reminder.WorkManagerReminderScheduler
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
    /** Non-null only in the default due-date view without filters; otherwise [tasks] is shown flat. */
    val sections: List<Pair<TaskSection, List<Task>>>? = null,
    val collapsedSections: Set<TaskSection> = emptySet(),
    val stats: TaskStats = TaskStats(),
    val filters: TaskFilters = TaskFilters(),
) {
    val hasAnyTasks: Boolean get() = stats.total > 0
}

/** One-off events the list screen turns into snackbars with an Undo action. */
sealed interface TaskEvent {
    data class Deleted(val task: Task) : TaskEvent
    /** [nextOccurrence] is the follow-up created when a repeating task was completed; undo removes it. */
    data class StatusChanged(val previous: Task, val newStatus: TaskStatus, val nextOccurrence: Task? = null) : TaskEvent
    data class ClearedCompleted(val tasks: List<Task>) : TaskEvent
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val reminders: ReminderScheduler = ReminderScheduler.None,
) : ViewModel() {

    private val filters = MutableStateFlow(TaskFilters())
    private val collapsed = MutableStateFlow(setOf<TaskSection>())

    val uiState: StateFlow<TaskListUiState> =
        combine(repository.allTasks, filters, collapsed) { tasks, filters, collapsed ->
            val now = System.currentTimeMillis()
            val visible = filters.apply(tasks, now)
            TaskListUiState(
                isLoading = false,
                tasks = visible,
                sections = if (filters.sort == SortOrder.DUE_SOONEST && !filters.isActive) groupIntoSections(visible, now) else null,
                collapsedSections = collapsed,
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

    fun toggleSection(section: TaskSection) = collapsed.update { if (section in it) it - section else it + section }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val previous = if (task.id != 0) repository.getTask(task.id) else null
            val cleaned = task.copy(title = task.title.trim(), description = task.description?.trim())
            val saved = cleaned.copy(id = repository.save(cleaned))
            reminders.sync(saved)
            // completing a repeating task from the editor schedules the next one, same as the checkbox
            if (previous != null && !previous.isCompleted && saved.isCompleted) {
                val next = spawnNextOccurrence(saved)
                if (next != null) _events.send(TaskEvent.StatusChanged(previous, TaskStatus.COMPLETED, next))
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            reminders.cancel(task.id)
            _events.send(TaskEvent.Deleted(task))
        }
    }

    fun toggleCompleted(task: Task) {
        val newStatus = if (task.isCompleted) TaskStatus.PENDING else TaskStatus.COMPLETED
        viewModelScope.launch {
            val updated = task.copy(status = newStatus.toString())
            repository.save(updated)
            reminders.sync(updated)
            val next = if (newStatus == TaskStatus.COMPLETED) spawnNextOccurrence(updated) else null
            _events.send(TaskEvent.StatusChanged(task, newStatus, next))
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
                is TaskEvent.Deleted -> restore(event.task)
                is TaskEvent.StatusChanged -> {
                    event.nextOccurrence?.let {
                        repository.delete(it)
                        reminders.cancel(it.id)
                    }
                    restore(event.previous)
                }
                is TaskEvent.ClearedCompleted -> event.tasks.forEach { repository.save(it) }
            }
        }
    }

    private suspend fun restore(task: Task) {
        repository.save(task)
        reminders.sync(task)
    }

    private suspend fun spawnNextOccurrence(completed: Task): Task? {
        val next = completed.nextOccurrence() ?: return null
        val saved = next.copy(id = repository.save(next))
        reminders.sync(saved)
        return saved
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                TaskViewModel(
                    RoomTaskRepository(TaskDatabase.getDatabase(app).taskDao()),
                    WorkManagerReminderScheduler(app),
                )
            }
        }
    }
}
