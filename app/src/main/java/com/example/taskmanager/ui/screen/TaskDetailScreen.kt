package com.example.taskmanager.ui.screen

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanager.database.Task
import com.example.taskmanager.ui.components.CircularRevealAnimation
import com.example.taskmanager.ui.components.CustomDatePickerField
import com.example.taskmanager.ui.components.PriorityDropdown
import com.example.taskmanager.ui.components.StatusDropdown
import com.example.taskmanager.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, viewModel: TaskViewModel, taskId: Int) {
    val task by viewModel.task.collectAsState()

    // State variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var status by remember { mutableStateOf("Pending") }

    val focusRequesterTitle = remember { FocusRequester() }
    val focusRequesterDescription = remember { FocusRequester() }
    val focusRequesterPriority = remember { FocusRequester() }
    val focusRequesterStatus = remember { FocusRequester() }
    val focusRequesterDate = remember { FocusRequester() }
    val focusRequesterButton = remember { FocusRequester() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current


    var isVisible by remember { mutableStateOf(false) }
    var isReverse by remember { mutableStateOf(false) }

    BackHandler {
        isReverse = true
    }

    LaunchedEffect(isReverse) {
        if (isReverse) {
            delay(500) // Wait for the animation to complete
            navController.popBackStack() // Navigate back
        }
    }

    BackHandler {

    }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequesterTitle.requestFocus()
    }

    LaunchedEffect(taskId) {
        viewModel.fetchTaskById(taskId.toString())
    }


    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description ?: ""
            priority = it.priority
            dueDate = it.dueDate
            status = it.status
        }
    }

    CircularRevealAnimation(
        isVisible = isVisible,
        reverse = isReverse,
        backgroundColor = MaterialTheme.colorScheme.primary,
        durationMillis = 500
    ) {

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Task Detail", color = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isReverse = true
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Handle settings click */ }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary // Dynamically changes with theme
                    )
                )

            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    minLines = 1,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequesterDescription.requestFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesterTitle)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                focusRequesterDescription.requestFocus()
                                true
                            } else false
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // **Description Input**
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesterDescription)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                focusRequesterPriority.requestFocus()
                                true
                            } else false
                        },

                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusRequesterPriority.requestFocus()
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(12.dp))

                // **Priority Dropdown**
                PriorityDropdown(
                    priority = priority,
                    onPriorityChange = { priority = it },
                    modifier = Modifier
                        .focusRequester(focusRequesterPriority)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                focusRequesterStatus.requestFocus()
                                true
                            } else false
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatusDropdown(status = status, onStatusChange = { status = it },
                    modifier = Modifier
                        .focusRequester(focusRequesterStatus)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                focusRequesterDate.requestFocus()
                                true
                            } else false
                        })

                Spacer(modifier = Modifier.height(12.dp))


                CustomDatePickerField(dueDate) { selectedDate ->
                    dueDate = selectedDate
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Task Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.updateTask(
                                Task(
                                    id = taskId,
                                    title = title,
                                    status = status,
                                    description = description,
                                    priority = priority,
                                    dueDate = dueDate
                                )
                            )
                            keyboardController?.hide()
                            isReverse = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary // Dynamically changes with theme
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesterButton)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                keyboardController?.hide()
                                true
                            } else false
                        },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Save Task", color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Save Task Button
                Button(
                    onClick = {
                        if (task != null) {
                            viewModel.deleteTask(
                                task!!
                            )
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Task Deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addTask(task!!)
                                } else {
                                    keyboardController?.hide()
                                    isReverse = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary // Dynamically changes with theme
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequesterButton)
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                keyboardController?.hide()
                                true
                            } else false
                        },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Delete Task", color = Color.White)
                }
            }
        }
        /*Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
        ) {

        }*/
    }
    
}
