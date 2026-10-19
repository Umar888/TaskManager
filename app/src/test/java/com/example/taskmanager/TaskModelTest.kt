package com.example.taskmanager

import com.example.taskmanager.data.model.Checklist
import com.example.taskmanager.data.model.ChecklistItem
import com.example.taskmanager.data.model.RepeatRule
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.ui.tasks.TaskSection
import com.example.taskmanager.ui.tasks.groupIntoSections
import com.example.taskmanager.util.DateUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class TaskModelTest {

    // fixed "now": Wed 15 Oct 2025, 14:00 local time
    private val now = Calendar.getInstance().apply { clear(); set(2025, Calendar.OCTOBER, 15, 14, 0) }.timeInMillis
    private fun at(dayOffset: Int, hour: Int = 12) = DateUtils.withTime(DateUtils.startOfDay(now, dayOffset), hour, 0)

    private fun dayOfWeek(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_WEEK)

    @Test
    fun `weekdays rule skips the weekend`() {
        val friday = at(2) // Fri 17 Oct
        assertThat(dayOfWeek(friday)).isEqualTo(Calendar.FRIDAY)
        assertThat(dayOfWeek(RepeatRule.WEEKDAYS.next(friday))).isEqualTo(Calendar.MONDAY)
    }

    @Test
    fun `monthly rule keeps the time of day`() {
        val next = RepeatRule.MONTHLY.next(at(0, hour = 9))
        assertThat(DateUtils.hourOf(next)).isEqualTo(9)
        assertThat(Calendar.getInstance().apply { timeInMillis = next }.get(Calendar.MONTH)).isEqualTo(Calendar.NOVEMBER)
    }

    @Test
    fun `next occurrence resets status and checklist and lands in the future`() {
        val task = Task(
            id = 7,
            title = "Water plants",
            description = null,
            dueDate = at(-3),
            status = TaskStatus.COMPLETED.toString(),
            repeat = RepeatRule.DAILY.toString(),
            checklist = Checklist.encode(listOf(ChecklistItem("Ferns", done = true))),
        )
        val next = task.nextOccurrence(now)!!
        assertThat(next.id).isEqualTo(0)
        assertThat(next.statusType).isEqualTo(TaskStatus.PENDING)
        assertThat(next.checklistItems.single().done).isFalse()
        // three days overdue: skips the missed days instead of creating another overdue copy
        assertThat(next.dueDate).isEqualTo(at(1))
    }

    @Test
    fun `non-repeating tasks have no next occurrence`() {
        assertThat(Task(title = "Once", description = null, dueDate = at(0)).nextOccurrence(now)).isNull()
    }

    @Test
    fun `checklist encoding round-trips and drops blank items`() {
        val items = listOf(ChecklistItem("Milk", done = true), ChecklistItem("  "), ChecklistItem("Eggs | bacon"))
        val decoded = Checklist.decode(Checklist.encode(items))
        assertThat(decoded).containsExactly(ChecklistItem("Milk", done = true), ChecklistItem("Eggs | bacon")).inOrder()
        assertThat(Checklist.encode(emptyList())).isNull()
    }

    @Test
    fun `reminder time is offset from the due date`() {
        val task = Task(title = "T", description = null, dueDate = at(1), reminderMinutes = 60)
        assertThat(task.reminderAt).isEqualTo(at(1) - 3_600_000)
        assertThat(task.copy(reminderMinutes = null).reminderAt).isNull()
    }

    @Test
    fun `tasks are grouped into ordered sections`() {
        val tasks = listOf(
            Task(1, "later", null, dueDate = at(5)),
            Task(2, "late", null, dueDate = at(-1)),
            Task(3, "today", null, dueDate = at(0, hour = 18)),
            Task(4, "done", null, dueDate = at(0), status = TaskStatus.COMPLETED.toString()),
            Task(5, "tomorrow", null, dueDate = at(1)),
        )
        val sections = groupIntoSections(tasks, now)
        assertThat(sections.map { it.first }).containsExactly(
            TaskSection.OVERDUE, TaskSection.TODAY, TaskSection.TOMORROW, TaskSection.UPCOMING, TaskSection.COMPLETED
        ).inOrder()
        assertThat(sections.map { it.second.single().id }).containsExactly(2, 3, 5, 1, 4).inOrder()
    }
}
