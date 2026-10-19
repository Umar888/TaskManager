package com.example.taskmanager.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

// priority/status stay as display strings so existing databases keep working without a migration
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val priority: String = TaskPriority.MEDIUM.toString(),
    val dueDate: Long,
    val status: String = TaskStatus.PENDING.toString(),
    /** Minutes before [dueDate] to notify; null means no reminder. */
    val reminderMinutes: Int? = null,
    @ColumnInfo(defaultValue = "None")
    val repeat: String = RepeatRule.NONE.toString(),
    /** Encoded with [Checklist.encode]; null when the task has no checklist. */
    val checklist: String? = null,
) {
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED.toString()

    val priorityLevel: TaskPriority get() = TaskPriority.from(priority)

    val statusType: TaskStatus get() = TaskStatus.from(status)

    val repeatRule: RepeatRule get() = RepeatRule.from(repeat)

    val checklistItems: List<ChecklistItem> get() = Checklist.decode(checklist)

    val reminderAt: Long? get() = reminderMinutes?.let { dueDate - it * 60_000L }

    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean = !isCompleted && dueDate < now

    /** The next occurrence of a repeating task: same details, next due date, fresh status and checklist. */
    fun nextOccurrence(now: Long = System.currentTimeMillis()): Task? {
        if (repeatRule == RepeatRule.NONE) return null
        var next = repeatRule.next(dueDate)
        // completing a long-overdue daily task shouldn't spawn another overdue copy
        while (next < now) next = repeatRule.next(next)
        return copy(
            id = 0,
            dueDate = next,
            status = TaskStatus.PENDING.toString(),
            checklist = Checklist.encode(checklistItems.map { it.copy(done = false) }),
        )
    }
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

enum class RepeatRule(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKDAYS("Weekdays"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    override fun toString(): String = displayName

    fun next(from: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        when (this) {
            NONE -> return from
            DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            WEEKDAYS -> do cal.add(Calendar.DAY_OF_YEAR, 1) while (
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            )
            WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            MONTHLY -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        fun from(value: String?): RepeatRule =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) } ?: NONE
    }
}

data class ChecklistItem(val text: String, val done: Boolean = false)

/** One item per line, prefixed with "x|" or " |". Plain text keeps the column readable and needs no extra library. */
object Checklist {
    fun encode(items: List<ChecklistItem>): String? = items
        .filter { it.text.isNotBlank() }
        .takeIf { it.isNotEmpty() }
        ?.joinToString("\n") { (if (it.done) "x|" else " |") + it.text.replace('\n', ' ').trim() }

    fun decode(value: String?): List<ChecklistItem> = value.orEmpty()
        .lines()
        .filter { it.length > 2 && it[1] == '|' }
        .map { ChecklistItem(text = it.substring(2), done = it[0] == 'x') }
}
