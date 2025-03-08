package com.example.taskmanager.repository

import com.example.taskmanager.database.Task
import com.example.taskmanager.database.TaskDao
import com.example.taskmanager.database.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }
    // Flow to observe all tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getTasksByStatus(TaskStatus.COMPLETED.toString())

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun getTaskById(id: String): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(id)
        }
    }



    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }
}
