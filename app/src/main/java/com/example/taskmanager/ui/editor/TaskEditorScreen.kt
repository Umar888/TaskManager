package com.example.taskmanager.ui.editor

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.data.model.Checklist
import com.example.taskmanager.data.model.ChecklistItem
import com.example.taskmanager.data.model.RepeatRule
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.TaskPriority
import com.example.taskmanager.data.model.TaskStatus
import com.example.taskmanager.reminder.ReminderNotifications
import com.example.taskmanager.util.DateUtils
import com.example.taskmanager.ui.theme.TaskColors
import com.example.taskmanager.ui.tasks.TaskViewModel
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar

/** Reminder choices in minutes before the due time; null = no reminder. */
private val reminderOptions = listOf(null, 0, 10, 60, 24 * 60)

private val ChecklistSaver = listSaver<List<ChecklistItem>, String>(
    save = { items -> items.map { (if (it.done) "x" else " ") + it.text } },
    restore = { saved -> saved.map { ChecklistItem(text = it.drop(1), done = it.first() == 'x') } }
)

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
    var reminderMinutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var repeat by rememberSaveable { mutableStateOf(RepeatRule.NONE) }
    var checklist by rememberSaveable(stateSaver = ChecklistSaver) { mutableStateOf(emptyList<ChecklistItem>()) }
    var loaded by rememberSaveable { mutableStateOf(taskId == null) }
    var showTitleError by rememberSaveable { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(ReminderNotifications.canPost(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = granted
    }

    // fill the form once when the task arrives, then leave the user's edits alone
    LaunchedEffect(existing) {
        val task = existing ?: return@LaunchedEffect
        if (!loaded) {
            title = task.title
            description = task.description.orEmpty()
            priority = task.priorityLevel
            status = task.statusType
            dueDate = task.dueDate
            reminderMinutes = task.reminderMinutes
            repeat = task.repeatRule
            checklist = task.checklistItems
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
        reminderMinutes = reminderMinutes,
        repeat = repeat.toString(),
        checklist = Checklist.encode(checklist),
    )
    val hasChanges = if (taskId == null) {
        title.isNotBlank() || description.isNotBlank() || checklist.any { it.text.isNotBlank() }
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

    fun chooseReminder(minutes: Int?) {
        reminderMinutes = minutes
        if (minutes != null && !notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    // imePadding on the whole Scaffold (not just the button) so the scroll area ends above the keyboard
    // and fields near the bottom can still be scrolled into view while typing
    Scaffold(
        modifier = Modifier.imePadding(),
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
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(56.dp)
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
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // big, borderless title + notes, like a note-taking app; the cards below hold the structured fields
            TextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) showTitleError = false
                },
                placeholder = { Text(stringResource(R.string.title_placeholder), style = MaterialTheme.typography.headlineSmall) },
                isError = showTitleError,
                supportingText = if (showTitleError) {
                    { Text(stringResource(R.string.title_required)) }
                } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = bareFieldColors(),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("TitleField")
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text(stringResource(R.string.description_placeholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = bareFieldColors(),
                minLines = 2,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("DescriptionField")
            )

            EditorCard(stringResource(R.string.details)) {
                FieldLabel(stringResource(R.string.priority))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TaskPriority.entries.forEach { option ->
                        val color = TaskColors.priority(option)
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option.displayName) },
                            leadingIcon = {
                                Icon(painterResource(R.drawable.ic_flag), contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                iconColor = color,
                                selectedContainerColor = color.copy(alpha = 0.18f),
                                selectedLabelColor = color,
                                selectedLeadingIconColor = color,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("EditorPriority_${option.displayName}")
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                FieldLabel(stringResource(R.string.status))
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

            EditorCard(stringResource(R.string.schedule)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PickerCard(
                        label = stringResource(R.string.due_date),
                        value = DateUtils.formatDate(dueDate),
                        icon = rememberVectorPainter(Icons.Default.DateRange),
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("DatePickerField")
                    )
                    PickerCard(
                        label = stringResource(R.string.due_time),
                        value = DateUtils.formatTime(dueDate),
                        icon = painterResource(R.drawable.ic_schedule),
                        onClick = { showTimePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("TimePickerField")
                    )
                }
                Spacer(Modifier.height(8.dp))
                ChipRow {
                    val now = System.currentTimeMillis()
                    AssistChip(onClick = { dueDate = keepTime(dueDate, now) }, label = { Text(stringResource(R.string.quick_today)) })
                    AssistChip(
                        onClick = { dueDate = keepTime(dueDate, DateUtils.plus(now, Calendar.DAY_OF_YEAR, 1)) },
                        label = { Text(stringResource(R.string.quick_tomorrow)) }
                    )
                    AssistChip(
                        onClick = { dueDate = keepTime(dueDate, DateUtils.plus(now, Calendar.WEEK_OF_YEAR, 1)) },
                        label = { Text(stringResource(R.string.quick_next_week)) }
                    )
                }

                Spacer(Modifier.height(16.dp))
                FieldLabel(stringResource(R.string.reminder), icon = rememberVectorPainter(Icons.Default.Notifications))
                ChipRow {
                    reminderOptions.forEach { minutes ->
                        FilterChip(
                            selected = reminderMinutes == minutes,
                            onClick = { chooseReminder(minutes) },
                            label = { Text(reminderLabel(minutes)) },
                            modifier = Modifier.testTag("ReminderOption_${minutes ?: "None"}")
                        )
                    }
                }
                if (reminderMinutes != null && !notificationsAllowed) {
                    Text(
                        stringResource(R.string.reminder_permission_denied),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                FieldLabel(stringResource(R.string.repeat), icon = painterResource(R.drawable.ic_repeat))
                ChipRow {
                    RepeatRule.entries.forEach { rule ->
                        FilterChip(
                            selected = repeat == rule,
                            onClick = { repeat = rule },
                            label = { Text(rule.displayName) },
                            modifier = Modifier.testTag("RepeatOption_${rule.name}")
                        )
                    }
                }
            }

            ChecklistCard(items = checklist, onChange = { checklist = it })
            Spacer(Modifier.height(8.dp))
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
private fun reminderLabel(minutes: Int?): String = stringResource(
    when (minutes) {
        null -> R.string.reminder_none
        0 -> R.string.reminder_at_time
        10 -> R.string.reminder_10_min
        60 -> R.string.reminder_1_hour
        else -> R.string.reminder_1_day
    }
)

@Composable
private fun bareFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
)

@Composable
private fun EditorCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp).animateContentSize()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun FieldLabel(text: String, icon: Painter? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) { content() }
}

@Composable
private fun PickerCard(label: String, value: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, shape = MaterialTheme.shapes.medium, modifier = modifier) {
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
private fun ChecklistCard(items: List<ChecklistItem>, onChange: (List<ChecklistItem>) -> Unit) {
    var newItem by rememberSaveable { mutableStateOf("") }
    fun addItem() {
        if (newItem.isBlank()) return
        onChange(items + ChecklistItem(newItem.trim()))
        newItem = ""
    }

    EditorCard(stringResource(R.string.checklist)) {
        if (items.isNotEmpty()) {
            val done = items.count { it.done }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                LinearProgressIndicator(
                    progress = { done.toFloat() / items.size },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.checklist_progress, done, items.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.done,
                    onCheckedChange = { checked -> onChange(items.toMutableList().also { it[index] = item.copy(done = checked) }) },
                    modifier = Modifier.testTag("ChecklistCheck_$index")
                )
                TextField(
                    value = item.text,
                    onValueChange = { text -> onChange(items.toMutableList().also { it[index] = item.copy(text = text) }) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.done) TextDecoration.LineThrough else null,
                        color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    ),
                    colors = bareFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onChange(items.filterIndexed { i, _ -> i != index }) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.checklist_remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = ::addItem, enabled = newItem.isNotBlank()) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.checklist_add))
            }
            TextField(
                value = newItem,
                onValueChange = { newItem = it },
                placeholder = { Text(stringResource(R.string.checklist_add)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addItem() }),
                colors = bareFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ChecklistNewItem")
            )
        }
    }
}
