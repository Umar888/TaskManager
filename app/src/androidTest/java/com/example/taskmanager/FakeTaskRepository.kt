package com.example.taskmanager

import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskRepository(initial: List<Task> = emptyList()) : TaskRepository {
    private val tasks = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override val allTasks: Flow<List<Task>> = tasks

    override fun observeTask(id: Int): Flow<Task?> = tasks.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getTask(id: Int): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun save(task: Task): Int {
        val id = if (task.id != 0) task.id else nextId++
        tasks.update { list ->
            if (list.any { it.id == id }) list.map { if (it.id == id) task else it } else list + task.copy(id = id)
        }
        return id
    }

    override suspend fun delete(task: Task) = tasks.update { list -> list.filterNot { it.id == task.id } }

    override suspend fun deleteCompleted() = tasks.update { list -> list.filterNot { it.isCompleted } }
}
