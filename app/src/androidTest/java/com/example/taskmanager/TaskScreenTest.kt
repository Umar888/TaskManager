package com.example.taskmanager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.ui.navigation.Screen
import com.example.taskmanager.ui.editor.TaskEditorScreen
import com.example.taskmanager.ui.tasks.TaskListScreen
import com.example.taskmanager.ui.theme.AppTheme
import com.example.taskmanager.ui.tasks.TaskViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        viewModel = TaskViewModel(FakeTaskRepository())
        composeTestRule.setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.TaskList.route) {
                    composable(Screen.TaskList.route) { TaskListScreen(navController, viewModel) }
                    composable(Screen.AddTask.route) { TaskEditorScreen(navController, viewModel, taskId = null) }
                    composable(Screen.TaskDetail.route) { entry ->
                        TaskEditorScreen(navController, viewModel, entry.arguments!!.getString("taskId")!!.toInt())
                    }
                }
            }
        }
    }

    private fun addTask(title: String, description: String, priority: String) {
        composeTestRule.onNodeWithTag("AddTask").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("TitleField").performTextInput(title)
        composeTestRule.onNodeWithTag("DescriptionField").performTextInput(description)
        composeTestRule.onNodeWithTag("EditorPriority_$priority").performClick()
        composeTestRule.onNodeWithTag("DatePickerField").performClick()
        composeTestRule.onNodeWithTag("DatePickerOK").performClick()
        composeTestRule.onNodeWithTag("SaveTask").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(title).assertExists()
    }

    private fun pick(menuTag: String, optionTag: String) {
        composeTestRule.onNodeWithTag(menuTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(optionTag).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun emptyState_isShownWithoutTasks() {
        composeTestRule.onNodeWithText("Stay productive! Start by adding a new task.").assertIsDisplayed()
    }

    @Test
    fun saveWithoutTitle_showsValidationError() {
        composeTestRule.onNodeWithTag("AddTask").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SaveTask").performClick()
        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
    }

    @Test
    fun createFilterAndSortTasks() {
        addTask("Mobile App", "This is mobile application task.", "High")
        addTask("Web App", "This is web application task.", "Medium")
        addTask("SEO", "This is SEO task.", "Low")

        // priority filter
        pick("PriorityFiltering", "PriorityOption_High")
        composeTestRule.onNodeWithText("Mobile App").assertExists()
        composeTestRule.onNodeWithText("Web App").assertDoesNotExist()
        pick("PriorityFiltering", "PriorityOption_All")

        // status filter: nothing completed yet
        composeTestRule.onNodeWithTag("StatusOption_Completed").performClick()
        composeTestRule.onNodeWithText("No matching tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear filters").performClick()
        composeTestRule.onNodeWithText("SEO").assertExists()

        // sorting
        pick("SortFiltering", "SortOption_PRIORITY")
        composeTestRule.onNodeWithTag("TaskItem_0").assertTextContains("Mobile App")
        pick("SortFiltering", "SortOption_NEWEST")
        composeTestRule.onNodeWithTag("TaskItem_0").assertTextContains("SEO")

        // search matches the description too
        composeTestRule.onNodeWithTag("SearchButton").performClick()
        composeTestRule.onNodeWithTag("SearchField").performTextInput("web application")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Web App").assertExists()
        composeTestRule.onNodeWithText("SEO").assertDoesNotExist()
    }

    @Test
    fun completingTask_movesItToCompletedFilter() {
        addTask("Write report", "Quarterly numbers", "High")

        composeTestRule.onNodeWithTag("TaskCheck_0").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("StatusOption_Completed").performClick()
        composeTestRule.onNodeWithText("Write report").assertExists()
    }

    @Test
    fun editAndDeleteTask() {
        addTask("Old title", "Some details", "Low")

        composeTestRule.onNodeWithText("Old title").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("TitleField").performTextReplacement("Old title updated")
        composeTestRule.onNodeWithTag("SaveTask").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Old title updated").assertExists()

        composeTestRule.onNodeWithText("Old title updated").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("DeleteTask").performClick()
        composeTestRule.onNodeWithTag("ConfirmDelete").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Old title updated").assertDoesNotExist()
        composeTestRule.onNodeWithText("Undo").assertExists()
    }
}
