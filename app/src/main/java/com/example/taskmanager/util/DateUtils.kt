package com.example.taskmanager.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun startOfDay(time: Long, dayOffset: Int = 0): Long = Calendar.getInstance().apply {
        timeInMillis = time
        add(Calendar.DAY_OF_YEAR, dayOffset)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun startOfWeek(time: Long): Long = Calendar.getInstance().apply {
        timeInMillis = startOfDay(time)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        if (timeInMillis > time) add(Calendar.WEEK_OF_YEAR, -1)
    }.timeInMillis

    fun startOfMonth(time: Long): Long = Calendar.getInstance().apply {
        timeInMillis = startOfDay(time)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    fun plus(time: Long, field: Int, amount: Int): Long = Calendar.getInstance().apply {
        timeInMillis = time
        add(field, amount)
    }.timeInMillis

    fun isSameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

    /** Material DatePicker hands back UTC midnight, so read the date parts in UTC and rebuild them locally. */
    fun combineUtcDateWithLocalTime(utcDateMillis: Long, localTime: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDateMillis }
        val time = Calendar.getInstance().apply { timeInMillis = localTime }
        return Calendar.getInstance().apply {
            clear()
            set(
                utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH),
                time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE)
            )
        }.timeInMillis
    }

    /** Inverse of the above: local date -> UTC midnight of the same calendar day, for seeding the picker. */
    fun toUtcDateMillis(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun withTime(millis: Long, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun hourOf(millis: Long): Int = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    fun minuteOf(millis: Long): Int = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MINUTE)

    fun formatDate(millis: Long): String =
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(millis))

    fun formatShortDate(millis: Long): String =
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))

    fun formatTime(millis: Long): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

    /** Default due date for a new task: end of today, so it doesn't instantly show as overdue. */
    fun defaultDueDate(now: Long = System.currentTimeMillis()): Long = withTime(now, 23, 59)
}
