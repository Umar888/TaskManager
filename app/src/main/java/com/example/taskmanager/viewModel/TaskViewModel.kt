package com.example.taskmanager.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.database.Task
import com.example.taskmanager.database.TaskDatabase
import com.example.taskmanager.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _completedTasks = MutableStateFlow<List<Task>>(emptyList())
    open val tasks: StateFlow<List<Task>> = _tasks
    val completedTasks: StateFlow<List<Task>> = _completedTasks

    private val _isLoading = MutableStateFlow(true) // Added loading state
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        viewModelScope.launch {
            repository.allTasks.collect {
                _tasks.value = it
                _isLoading.value = false
            }
            repository.completedTasks.collect {
                _completedTasks.value = it
            }
        }
    }


    open fun addTask(task: Task) {
        viewModelScope.launch { repository.insert(task) }
    }


    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    fun fetchTaskById(id: String) {
        viewModelScope.launch {
            _task.value = repository.getTaskById(id)
        }
    }


    open fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
            _completedTasks.value = _completedTasks.value.map { if (it.id == task.id) task else it }
        }
    }


}

