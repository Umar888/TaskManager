package com.example.taskmanager.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.taskmanager.data.model.Task
import java.util.concurrent.TimeUnit

/** Keeps a task's notification in sync with its reminder setting. Interface so tests don't need WorkManager. */
interface ReminderScheduler {
    /** Schedules, reschedules or cancels the reminder for [task] depending on its current state. */
    fun sync(task: Task)
    fun cancel(taskId: Int)

    object None : ReminderScheduler {
        override fun sync(task: Task) = Unit
        override fun cancel(taskId: Int) = Unit
    }
}

class WorkManagerReminderScheduler(context: Context) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun sync(task: Task) {
        val triggerAt = task.reminderAt
        val delay = triggerAt?.minus(System.currentTimeMillis())
        if (task.id == 0 || task.isCompleted || delay == null || delay <= 0) {
            cancel(task.id)
            return
        }
        // WorkManager persists across reboots, so there's no boot receiver to maintain
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_TASK_ID to task.id, ReminderWorker.KEY_TRIGGER_AT to triggerAt))
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(workName(task.id), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(taskId: Int) {
        workManager.cancelUniqueWork(workName(taskId))
    }

    private fun workName(taskId: Int) = "task-reminder-$taskId"

    private companion object {
        const val TAG = "task-reminder"
    }
}
