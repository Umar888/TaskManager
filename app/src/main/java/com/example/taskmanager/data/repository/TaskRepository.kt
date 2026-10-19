package com.example.taskmanager.data.repository

import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.local.TaskDao
import com.example.taskmanager.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

// Interface so UI tests can swap in an in-memory implementation instead of Room
interface TaskRepository {
    val allTasks: Flow<List<Task>>
    fun observeTask(id: Int): Flow<Task?>
    suspend fun getTask(id: Int): Task?
    /** Inserts or updates [task] and returns its id (newly generated for inserts). */
    suspend fun save(task: Task): Int
    suspend fun delete(task: Task)
    suspend fun deleteCompleted()
}

class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {
    override val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    override fun observeTask(id: Int): Flow<Task?> = taskDao.observeTask(id)

    override suspend fun getTask(id: Int): Task? = taskDao.getTask(id)

    override suspend fun save(task: Task): Int {
        val rowId = taskDao.upsertTask(task)
        return if (rowId == -1L) task.id else rowId.toInt()
    }

    override suspend fun delete(task: Task) = taskDao.deleteTask(task)

    override suspend fun deleteCompleted() = taskDao.deleteTasksByStatus(TaskStatus.COMPLETED.toString())
}
