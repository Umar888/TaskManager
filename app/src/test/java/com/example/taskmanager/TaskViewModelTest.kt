package com.example.taskmanager

import com.example.taskmanager.data.model.RepeatRule
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.data.repository.TaskRepository
import com.example.taskmanager.reminder.ReminderScheduler
import com.example.taskmanager.ui.tasks.TaskEvent
import com.example.taskmanager.ui.tasks.TaskViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private class InMemoryRepo(initial: List<Task>) : TaskRepository {
        val tasks = MutableStateFlow(initial)
        override val allTasks: Flow<List<Task>> = tasks
        override fun observeTask(id: Int) = tasks.map { list -> list.firstOrNull { it.id == id } }
        override suspend fun getTask(id: Int) = tasks.value.firstOrNull { it.id == id }
        override suspend fun save(task: Task): Int {
            val id = if (task.id != 0) task.id else (tasks.value.maxOfOrNull { it.id } ?: 0) + 1
            tasks.update { list -> list.filterNot { it.id == id } + task.copy(id = id) }
            return id
        }
        override suspend fun delete(task: Task) = tasks.update { list -> list.filterNot { it.id == task.id } }
        override suspend fun deleteCompleted() = tasks.update { list -> list.filterNot { it.isCompleted } }
    }

    private val due = System.currentTimeMillis() + 86_400_000
    private val open = Task(1, "Open", null, dueDate = due)
    private val done = Task(2, "Done", null, dueDate = due, status = TaskStatus.COMPLETED.toString())

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `delete then undo restores the task`() = runTest {
        val repo = InMemoryRepo(listOf(open))
        val vm = TaskViewModel(repo)

        vm.deleteTask(open)
        val event = vm.events.first()
        assertThat(repo.tasks.value).isEmpty()

        vm.undo(event)
        assertThat(repo.tasks.value).containsExactly(open)
    }

    @Test
    fun `toggle complete flips status and undo reverts it`() = runTest {
        val repo = InMemoryRepo(listOf(open))
        val vm = TaskViewModel(repo)

        vm.toggleCompleted(open)
        val event = vm.events.first() as TaskEvent.StatusChanged
        assertThat(event.newStatus).isEqualTo(TaskStatus.COMPLETED)
        assertThat(repo.tasks.value.single().isCompleted).isTrue()

        vm.undo(event)
        assertThat(repo.tasks.value.single().isCompleted).isFalse()
    }

    @Test
    fun `clear completed undo also restores tasks hidden by the active filter`() = runTest {
        val repo = InMemoryRepo(listOf(open, done))
        val vm = TaskViewModel(repo)
        vm.setStatusFilter(TaskStatus.PENDING) // "Done" isn't visible

        vm.clearCompleted()
        val event = vm.events.first() as TaskEvent.ClearedCompleted
        assertThat(event.tasks).containsExactly(done)
        assertThat(repo.tasks.value).containsExactly(open)

        vm.undo(event)
        assertThat(repo.tasks.value).containsExactly(open, done)
    }

    @Test
    fun `save trims whitespace`() = runTest {
        val repo = InMemoryRepo(emptyList())
        TaskViewModel(repo).saveTask(Task(5, "  Title  ", "  notes ", dueDate = due))
        assertThat(repo.tasks.value.single().title).isEqualTo("Title")
        assertThat(repo.tasks.value.single().description).isEqualTo("notes")
    }

    private class RecordingScheduler : ReminderScheduler {
        val synced = mutableListOf<Task>()
        val cancelled = mutableListOf<Int>()
        override fun sync(task: Task) { synced += task }
        override fun cancel(taskId: Int) { cancelled += taskId }
    }

    @Test
    fun `completing a repeating task creates the next one and undo removes it`() = runTest {
        val daily = open.copy(repeat = RepeatRule.DAILY.toString())
        val repo = InMemoryRepo(listOf(daily))
        val vm = TaskViewModel(repo)

        vm.toggleCompleted(daily)
        val event = vm.events.first() as TaskEvent.StatusChanged
        val next = event.nextOccurrence!!
        assertThat(repo.tasks.value).hasSize(2)
        assertThat(next.isCompleted).isFalse()
        assertThat(next.dueDate).isEqualTo(RepeatRule.DAILY.next(daily.dueDate))

        vm.undo(event)
        assertThat(repo.tasks.value).containsExactly(daily)
    }

    @Test
    fun `completing a one-off task creates nothing`() = runTest {
        val repo = InMemoryRepo(listOf(open))
        val vm = TaskViewModel(repo)

        vm.toggleCompleted(open)
        val event = vm.events.first() as TaskEvent.StatusChanged
        assertThat(event.nextOccurrence).isNull()
        assertThat(repo.tasks.value).hasSize(1)
    }

    @Test
    fun `saving syncs the reminder with the generated id and delete cancels it`() = runTest {
        val repo = InMemoryRepo(emptyList())
        val scheduler = RecordingScheduler()
        val vm = TaskViewModel(repo, scheduler)

        vm.saveTask(Task(title = "Call", description = null, dueDate = due, reminderMinutes = 10))
        val saved = repo.tasks.value.single()
        assertThat(scheduler.synced.single().id).isEqualTo(saved.id)

        vm.deleteTask(saved)
        assertThat(scheduler.cancelled).containsExactly(saved.id)
    }
}
