package com.example.taskmanager

import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.util.DateUtils
import com.example.taskmanager.ui.tasks.DateFilter
import com.example.taskmanager.ui.tasks.SortOrder
import com.example.taskmanager.ui.tasks.TaskFilters
import com.example.taskmanager.ui.tasks.TaskStats
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class TaskFiltersTest {

    // fixed "now": Wed 15 Oct 2025, 14:00 local time
    private val now = Calendar.getInstance().apply { clear(); set(2025, Calendar.OCTOBER, 15, 14, 0) }.timeInMillis
    private fun at(dayOffset: Int, hour: Int = 12) = DateUtils.withTime(DateUtils.startOfDay(now, dayOffset), hour, 0)

    private fun task(
        id: Int,
        title: String = "Task $id",
        due: Long = at(1),
        priority: TaskPriority = TaskPriority.MEDIUM,
        status: TaskStatus = TaskStatus.PENDING,
        description: String? = null,
    ) = Task(id, title, description, priority.toString(), due, status.toString())

    @Test
    fun `date filter today includes earlier today but not yesterday`() {
        val earlierToday = task(1, due = at(0, hour = 1))
        val yesterday = task(2, due = at(-1))
        val result = TaskFilters(date = DateFilter.TODAY).apply(listOf(earlierToday, yesterday), now)
        assertThat(result).containsExactly(earlierToday)
    }

    @Test
    fun `this week covers the start of the week, which the old filter missed`() {
        val weekStart = DateUtils.startOfWeek(now)
        val firstMorning = task(1, due = weekStart + 60_000)
        val nextWeek = task(2, due = at(8))
        val result = TaskFilters(date = DateFilter.THIS_WEEK).apply(listOf(firstMorning, nextWeek), now)
        assertThat(result).containsExactly(firstMorning)
    }

    @Test
    fun `overdue only matches unfinished past tasks`() {
        val late = task(1, due = at(-2))
        val lateButDone = task(2, due = at(-2), status = TaskStatus.COMPLETED)
        val future = task(3, due = at(2))
        val result = TaskFilters(date = DateFilter.OVERDUE).apply(listOf(late, lateButDone, future), now)
        assertThat(result).containsExactly(late)
    }

    @Test
    fun `search matches title or description, ignoring case`() {
        val byTitle = task(1, title = "Buy Milk")
        val byDescription = task(2, title = "Errands", description = "milk and eggs")
        val other = task(3, title = "Gym")
        val result = TaskFilters(query = "  MILK ").apply(listOf(byTitle, byDescription, other), now)
        assertThat(result).containsExactly(byTitle, byDescription)
    }

    @Test
    fun `priority sort puts high first and completed tasks always last`() {
        val low = task(1, priority = TaskPriority.LOW)
        val highDone = task(2, priority = TaskPriority.HIGH, status = TaskStatus.COMPLETED)
        val high = task(3, priority = TaskPriority.HIGH)
        val medium = task(4, priority = TaskPriority.MEDIUM)
        val result = TaskFilters(sort = SortOrder.PRIORITY).apply(listOf(low, highDone, high, medium), now)
        assertThat(result).containsExactly(high, medium, low, highDone).inOrder()
    }

    @Test
    fun `due soonest and due latest are mirror images`() {
        val a = task(1, due = at(1))
        val b = task(2, due = at(3))
        val c = task(3, due = at(2))
        assertThat(TaskFilters(sort = SortOrder.DUE_SOONEST).apply(listOf(a, b, c), now)).containsExactly(a, c, b).inOrder()
        assertThat(TaskFilters(sort = SortOrder.DUE_LATEST).apply(listOf(a, b, c), now)).containsExactly(b, c, a).inOrder()
    }

    @Test
    fun `stats count each status and overdue`() {
        val stats = TaskStats.from(
            listOf(
                task(1, status = TaskStatus.PENDING, due = at(-1)),
                task(2, status = TaskStatus.IN_PROGRESS),
                task(3, status = TaskStatus.COMPLETED),
                task(4, status = TaskStatus.COMPLETED),
            ),
            now
        )
        assertThat(stats).isEqualTo(TaskStats(total = 4, pending = 1, inProgress = 1, completed = 2, overdue = 1))
        assertThat(stats.progress).isEqualTo(0.5f)
    }

    @Test
    fun `isActive ignores sort order`() {
        assertThat(TaskFilters(sort = SortOrder.NEWEST).isActive).isFalse()
        assertThat(TaskFilters(priority = TaskPriority.LOW).isActive).isTrue()
    }

    @Test
    fun `unknown stored strings fall back to sane defaults`() {
        val legacy = Task(1, "x", null, priority = "urgent", dueDate = now, status = "???")
        assertThat(legacy.priorityLevel).isEqualTo(TaskPriority.MEDIUM)
        assertThat(legacy.statusType).isEqualTo(TaskStatus.PENDING)
    }
}
