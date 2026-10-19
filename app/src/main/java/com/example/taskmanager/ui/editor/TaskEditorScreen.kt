package com.example.taskmanager.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.util.DateUtils
import com.example.taskmanager.ui.theme.TaskColors
import com.example.taskmanager.ui.tasks.TaskViewModel
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar

/** Add (taskId == null) and edit share one form so the two can't drift apart again. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(navController: NavController, viewModel: TaskViewModel, taskId: Int?) {
    val existing by remember(taskId) { taskId?.let(viewModel::observeTask) ?: flowOf(null) }
        .collectAsStateWithLifecycle(initialValue = null)

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var status by rememberSaveable { mutableStateOf(TaskStatus.PENDING) }
    var dueDate by rememberSaveable { mutableStateOf(DateUtils.defaultDueDate()) }
    var loaded by rememberSaveable { mutableStateOf(taskId == null) }
    var showTitleError by rememberSaveable { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    // fill the form once when the task arrives, then leave the user's edits alone
    LaunchedEffect(existing) {
        val task = existing ?: return@LaunchedEffect
        if (!loaded) {
            title = task.title
            description = task.description.orEmpty()
            priority = task.priorityLevel
            status = task.statusType
            dueDate = task.dueDate
            loaded = true
        }
    }

    val draft = Task(
        id = taskId ?: 0,
        title = title,
        description = description.ifBlank { null },
        priority = priority.toString(),
        dueDate = dueDate,
        status = status.toString(),
    )
    val hasChanges = if (taskId == null) {
        title.isNotBlank() || description.isNotBlank()
    } else {
        existing?.let { it.copy(description = it.description?.ifBlank { null }) != draft } ?: false
    }

    fun close() {
        keyboard?.hide()
        navController.popBackStack()
    }

    fun save() {
        if (title.isBlank()) {
            showTitleError = true
            return
        }
        viewModel.saveTask(draft)
        close()
    }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (taskId == null) R.string.new_task else R.string.edit_task)) },
                navigationIcon = {
                    IconButton(onClick = { if (hasChanges) showDiscardDialog = true else close() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (taskId != null && existing != null) {
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.testTag("DeleteTask")) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_task))
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = ::save,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                        .height(52.dp)
                        .testTag("SaveTask")
                ) {
                    Text(stringResource(R.string.save_task), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) showTitleError = false
                },
                label = { Text(stringResource(R.string.title_label)) },
                isError = showTitleError,
                supportingText = if (showTitleError) {
                    { Text(stringResource(R.string.title_required)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                textStyle = MaterialTheme.typography.titleMedium,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("TitleField")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 3,
                maxLines = 8,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("DescriptionField")
            )

            Section(stringResource(R.string.priority)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TaskPriority.entries.forEachIndexed { index, option ->
                        val color = TaskColors.priority(option)
                        SegmentedButton(
                            selected = priority == option,
                            onClick = { priority = option },
                            shape = SegmentedButtonDefaults.itemShape(index, TaskPriority.entries.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = color.copy(alpha = 0.18f),
                                activeContentColor = color,
                            ),
                            modifier = Modifier.testTag("EditorPriority_${option.displayName}")
                        ) { Text(option.displayName) }
                    }
                }
            }

            Section(stringResource(R.string.status)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TaskStatus.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = status == option,
                            onClick = { status = option },
                            shape = SegmentedButtonDefaults.itemShape(index, TaskStatus.entries.size),
                            modifier = Modifier.testTag("EditorStatus_${option.displayName}")
                        ) { Text(option.displayName, maxLines = 1) }
                    }
                }
            }

            Section(stringResource(R.string.due)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PickerCard(
                        label = stringResource(R.string.due_date),
                        value = DateUtils.formatDate(dueDate),
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("DatePickerField")
                    )
                    PickerCard(
                        label = stringResource(R.string.due_time),
                        value = DateUtils.formatTime(dueDate),
                        icon = Icons.Default.Notifications,
                        onClick = { showTimePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("TimePickerField")
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    val now = System.currentTimeMillis()
                    QuickDateChip(stringResource(R.string.quick_today)) { dueDate = keepTime(dueDate, now) }
                    QuickDateChip(stringResource(R.string.quick_tomorrow)) {
                        dueDate = keepTime(dueDate, DateUtils.plus(now, Calendar.DAY_OF_YEAR, 1))
                    }
                    QuickDateChip(stringResource(R.string.quick_next_week)) {
                        dueDate = keepTime(dueDate, DateUtils.plus(now, Calendar.WEEK_OF_YEAR, 1))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val todayUtc = DateUtils.toUtcDateMillis(System.currentTimeMillis())
        val currentUtc = DateUtils.toUtcDateMillis(dueDate)
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentUtc,
            selectableDates = object : SelectableDates {
                // past days are off limits, but keep an existing overdue date selectable
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minOf(todayUtc, currentUtc)
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { dueDate = DateUtils.combineUtcDateWithLocalTime(it, dueDate) }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("DatePickerOK")
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = DateUtils.hourOf(dueDate),
            initialMinute = DateUtils.minuteOf(dueDate),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.due_time)) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = DateUtils.withTime(dueDate, timeState.hour, timeState.minute)
                        showTimePicker = false
                    },
                    modifier = Modifier.testTag("TimePickerOK")
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body, existing?.title.orEmpty())) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        existing?.let(viewModel::deleteTask)
                        close()
                    },
                    modifier = Modifier.testTag("ConfirmDelete")
                ) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    close()
                }) { Text(stringResource(R.string.discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text(stringResource(R.string.keep_editing)) }
            }
        )
    }
}

private fun keepTime(current: Long, day: Long): Long =
    DateUtils.withTime(day, DateUtils.hourOf(current), DateUtils.minuteOf(current))

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun PickerCard(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun QuickDateChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}
