package com.example.taskmanager.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmanager.MainActivity
import com.example.taskmanager.R
import com.example.taskmanager.data.local.TaskDatabase
import com.example.taskmanager.util.DateUtils

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt(KEY_TASK_ID, 0)
        val task = TaskDatabase.getDatabase(applicationContext).taskDao().getTask(taskId) ?: return Result.success()
        // the task may have been completed or rescheduled since this reminder was queued
        if (task.isCompleted || task.reminderAt != inputData.getLong(KEY_TRIGGER_AT, -1)) return Result.success()

        ReminderNotifications.show(applicationContext, taskId, task.title, DateUtils.formatDateTime(task.dueDate))
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "taskId"
        const val KEY_TRIGGER_AT = "triggerAt"
    }
}

object ReminderNotifications {
    private const val CHANNEL_ID = "task_reminders"

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, taskId: Int, title: String, dueText: String) {
        if (!canPost(context)) return
        ensureChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            taskId,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.reminder_due, dueText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(taskId, notification)
        } catch (_: SecurityException) {
            // permission revoked between the check and the post; nothing useful to do
        }
    }

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
