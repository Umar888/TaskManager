package com.example.taskmanager

import com.example.taskmanager.util.DateUtils
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilsTest {

    private val originalZone = TimeZone.getDefault()

    @After
    fun restoreZone() = TimeZone.setDefault(originalZone)

    @Test
    fun `picker UTC date keeps the chosen day in a negative-offset timezone`() {
        // the old code read UTC midnight in local time, which lands on the previous day west of UTC
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2025, Calendar.MARCH, 10) }.timeInMillis
        val time = Calendar.getInstance().apply { clear(); set(2025, Calendar.JANUARY, 1, 9, 30) }.timeInMillis

        val result = Calendar.getInstance().apply { timeInMillis = DateUtils.combineUtcDateWithLocalTime(utcMidnight, time) }

        assertThat(result.get(Calendar.DAY_OF_MONTH)).isEqualTo(10)
        assertThat(result.get(Calendar.HOUR_OF_DAY)).isEqualTo(9)
        assertThat(result.get(Calendar.MINUTE)).isEqualTo(30)
    }

    @Test
    fun `toUtcDateMillis round trips with combineUtcDateWithLocalTime`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"))
        val local = Calendar.getInstance().apply { clear(); set(2025, Calendar.DECEMBER, 31, 23, 15) }.timeInMillis
        assertThat(DateUtils.combineUtcDateWithLocalTime(DateUtils.toUtcDateMillis(local), local)).isEqualTo(local)
    }

    @Test
    fun `startOfWeek is never after the given time`() {
        val sunday = Calendar.getInstance().apply { clear(); set(2025, Calendar.OCTOBER, 19, 10, 0) }.timeInMillis
        assertThat(DateUtils.startOfWeek(sunday)).isAtMost(sunday)
    }
}
