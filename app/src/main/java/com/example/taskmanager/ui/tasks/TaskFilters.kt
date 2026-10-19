package com.example.taskmanager.ui.tasks

import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.util.DateUtils
import java.util.Calendar

enum class DateFilter(val displayName: String) {
    ALL("Any date"),
    OVERDUE("Overdue"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month");

    fun matches(task: Task, now: Long): Boolean {
        val due = task.dueDate
        return when (this) {
            ALL -> true
            OVERDUE -> task.isOverdue(now)
            TODAY -> DateUtils.isSameDay(due, now)
            TOMORROW -> DateUtils.isSameDay(due, DateUtils.plus(now, Calendar.DAY_OF_YEAR, 1))
            THIS_WEEK -> DateUtils.startOfWeek(now).let { due >= it && due < DateUtils.plus(it, Calendar.WEEK_OF_YEAR, 1) }
            THIS_MONTH -> DateUtils.startOfMonth(now).let { due >= it && due < DateUtils.plus(it, Calendar.MONTH, 1) }
        }
    }
}

enum class SortOrder(val displayName: String) {
    DUE_SOONEST("Due soonest"),
    DUE_LATEST("Due latest"),
    PRIORITY("Priority"),
    NEWEST("Recently added");

    val comparator: Comparator<Task>
        get() = when (this) {
            DUE_SOONEST -> compareBy { it.dueDate }
            DUE_LATEST -> compareByDescending { it.dueDate }
            PRIORITY -> compareByDescending<Task> { it.priorityLevel.weight }.thenBy { it.dueDate }
            // ids are autoincrement, so a higher id means added later
            NEWEST -> compareByDescending { it.id }
        }
}

data class TaskFilters(
    val query: String = "",
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val date: DateFilter = DateFilter.ALL,
    val sort: SortOrder = SortOrder.DUE_SOONEST,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || status != null || priority != null || date != DateFilter.ALL

    fun apply(tasks: List<Task>, now: Long): List<Task> = tasks
        .filter { task ->
            (status == null || task.statusType == status) &&
                (priority == null || task.priorityLevel == priority) &&
                date.matches(task, now) &&
                (query.isBlank() ||
                    task.title.contains(query.trim(), ignoreCase = true) ||
                    task.description.orEmpty().contains(query.trim(), ignoreCase = true))
        }
        // completed tasks sink to the bottom so the open work stays on top
        .sortedWith(compareBy<Task> { it.isCompleted }.then(sort.comparator))
}

data class TaskStats(
    val total: Int = 0,
    val pending: Int = 0,
    val inProgress: Int = 0,
    val completed: Int = 0,
    val overdue: Int = 0,
) {
    val progress: Float get() = if (total == 0) 0f else completed.toFloat() / total

    companion object {
        fun from(tasks: List<Task>, now: Long) = TaskStats(
            total = tasks.size,
            pending = tasks.count { it.statusType == TaskStatus.PENDING },
            inProgress = tasks.count { it.statusType == TaskStatus.IN_PROGRESS },
            completed = tasks.count { it.isCompleted },
            overdue = tasks.count { it.isOverdue(now) },
        )
    }
}
