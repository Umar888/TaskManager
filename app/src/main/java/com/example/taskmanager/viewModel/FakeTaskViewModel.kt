package com.example.taskmanager.viewModel

import android.app.Application
import com.example.taskmanager.database.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTaskViewModel(application: Application) : TaskViewModel(application) {

    private val _fakeTasks = MutableStateFlow<List<Task>>(emptyList())
    override val tasks: StateFlow<List<Task>> = _fakeTasks
    private var taskCounter = 0

    fun generateUniqueTaskId(): Int {
        return taskCounter++
    }

    override fun addTask(task: Task) {
        val newTask = task.copy(id = generateUniqueTaskId())
        _fakeTasks.value = _fakeTasks.value + newTask
    }

    override fun deleteTask(task: Task) {
        _fakeTasks.value = _fakeTasks.value.filter { it.id != task.id }
    }
}



