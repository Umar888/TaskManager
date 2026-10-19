package com.example.taskmanager.data.local

import com.example.taskmanager.data.model.Task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    /** Returns the new row id on insert, or -1 when an existing row was updated. */
    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE status = :status")
    suspend fun deleteTasksByStatus(status: String)

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeTask(id: Int): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Int): Task?

    @Query("SELECT * FROM tasks WHERE reminderMinutes IS NOT NULL AND status != :completed")
    suspend fun getTasksWithReminders(completed: String): List<Task>
}
