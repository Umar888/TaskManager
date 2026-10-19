package com.example.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// priority/status stay as display strings so existing databases keep working without a migration
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val priority: String = TaskPriority.MEDIUM.toString(),
    val dueDate: Long,
    val status: String = TaskStatus.PENDING.toString()
) {
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED.toString()

    val priorityLevel: TaskPriority get() = TaskPriority.from(priority)

    val statusType: TaskStatus get() = TaskStatus.from(status)

    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean = !isCompleted && dueDate < now
}

enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    override fun toString(): String = displayName

    companion object {
        fun from(value: String): TaskStatus =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) } ?: PENDING
    }
}

enum class TaskPriority(val displayName: String, val weight: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2);

    override fun toString(): String = displayName

    companion object {
        fun from(value: String): TaskPriority =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}
