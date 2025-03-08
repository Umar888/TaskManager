package com.example.taskmanager.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CustomDatePickerField(dueDate: Long, onDateSelected: (Long) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) // Include time
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = LocalContentColor.current,
        disabledLabelColor = MaterialTheme.colorScheme.primary,
        disabledBorderColor = MaterialTheme.colorScheme.primary,
        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
    )

    Box(
        modifier = Modifier
            .testTag("DatePickerField")
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = dateFormat.format(Date(dueDate)), // Show Date + Time
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Due Date") },
            trailingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            },
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { showDatePicker = true }
                }
                .focusable(false)
        )
    }

    if (showDatePicker) {
        ThemedDatePickerDialog(
            showDialog = showDatePicker,
            onDismiss = { showDatePicker = false },
            minDate = System.currentTimeMillis(),
            selectedDate = selectedDate,

            onDateSelected = { selected ->
                showDatePicker = false
                selectedDate = selected
                val currentTime = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }
                val selectedCalendar = Calendar.getInstance().apply {
                    timeInMillis = selected
                    set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE))
                    set(Calendar.SECOND, currentTime.get(Calendar.SECOND))
                }
                onDateSelected(selectedCalendar.timeInMillis)
            }
        )
    }
}




