package com.example.taskmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Transaction
    suspend fun updateTask(task: Task) {
        val existingTask = getTaskById(task.id.toString())

        if (existingTask == null) {
            insertTask(task)
        } else {
            updateTasker(task)
        }
    }
    @Update
    suspend fun updateTasker(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>


    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Task?

    @Query("SELECT * FROM tasks WHERE status = :taskStatus")
    fun getTasksByStatus(taskStatus: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priority = :priority")
    fun getTasksByPriority(priority: String): Flow<List<Task>>
}
