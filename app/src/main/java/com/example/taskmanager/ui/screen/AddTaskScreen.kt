package com.example.taskmanager.ui.screen

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.taskmanager.ui.components.CustomDatePickerField
import com.example.taskmanager.ui.components.PriorityDropdown
import com.example.taskmanager.viewModel.TaskViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val focusRequesterTitle = remember { FocusRequester() }
    val focusRequesterDescription = remember { FocusRequester() }
    val focusRequesterPriority = remember { FocusRequester() }
    val focusRequesterDate = remember { FocusRequester() }
    val focusRequesterButton = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequesterTitle.requestFocus()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Task", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
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
            // **Title Input**
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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                singleLine = false,

                keyboardActions = KeyboardActions(
                    onDone = {
                        focusRequesterPriority.requestFocus()
                    }
                ),
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
                            focusRequesterDate.requestFocus()
                            true
                        } else false
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // **Date Picker**
            CustomDatePickerField(dueDate) { selectedDate ->
                dueDate = selectedDate
            }
            Spacer(modifier = Modifier.height(24.dp))

            // **Save Task Button**
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTask(
                            Task(title = title, description = description, priority = priority, dueDate = dueDate)
                        )
                        keyboardController?.hide()
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
        }
    }
}

