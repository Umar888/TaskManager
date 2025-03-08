package com.example.taskmanager

import android.app.Application
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.ui.screen.AddTaskScreen
import com.example.taskmanager.ui.screen.TaskListScreen
import com.example.taskmanager.viewModel.FakeTaskViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeTaskViewModel


    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        fakeViewModel = FakeTaskViewModel(application)
    }




    @Test
    fun testTaskFlow() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "task_list"
            ) {
                composable("task_list") {
                    TaskListScreen(navController = navController, viewModel = fakeViewModel)
                }
                composable("add_task") {
                    AddTaskScreen(navController = navController, viewModel = fakeViewModel)
                }
            }
        }

        // create task 1

        composeTestRule.onNodeWithTag("AddTask").performClick()
        composeTestRule.onNodeWithText("Title *").performTextInput("Mobile App")
        composeTestRule.onNodeWithText("Description").performTextInput("This is mobile application task.")
        composeTestRule.onNodeWithTag("PriorityDropdown").performClick()
        composeTestRule.onNodeWithText("High").performClick()
        composeTestRule.onNodeWithTag("DatePickerField").performClick()
        composeTestRule.onNodeWithTag("DatePickerOK").performClick()
        composeTestRule.onNodeWithText("Save Task").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mobile App").assertExists()

        // create task 2

        composeTestRule.onNodeWithTag("AddTask").performClick()
        composeTestRule.onNodeWithText("Title *").performTextInput("Web App")
        composeTestRule.onNodeWithText("Description").performTextInput("This is web application task.")
        composeTestRule.onNodeWithTag("PriorityDropdown").performClick()
        composeTestRule.onNode(
            hasText("Medium") and hasParent(hasTestTag("PriorityDropdown"))
        ).performClick()

        composeTestRule.onNodeWithTag("DatePickerField").performClick()
        composeTestRule.onNodeWithTag("DatePickerOK").performClick()
        composeTestRule.onNodeWithText("Save Task").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Web App").assertExists()

        // create task 3

        composeTestRule.onNodeWithTag("AddTask").performClick()
        composeTestRule.onNodeWithText("Title *").performTextInput("SEO")
        composeTestRule.onNodeWithText("Description").performTextInput("This is SEO task.")
        composeTestRule.onNodeWithTag("PriorityDropdown").performClick()
        composeTestRule.onNodeWithText("Low").performClick()
        composeTestRule.onNodeWithTag("DatePickerField").performClick()
        composeTestRule.onNodeWithTag("DatePickerOK").performClick()
        composeTestRule.onNodeWithText("Save Task").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("SEO").assertExists()

        // Priority Filtering

        composeTestRule.onNodeWithTag("PriorityFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("PriorityOption_High").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mobile App").assertExists()

        composeTestRule.onNodeWithTag("PriorityFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("PriorityOption_Medium").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Web App").assertExists()

        composeTestRule.onNodeWithTag("PriorityFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("PriorityOption_Low").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("SEO").assertExists()
        composeTestRule.onNodeWithTag("PriorityFiltering").performClick()
        composeTestRule.onNodeWithTag("PriorityOption_All").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Status Filtering

        composeTestRule.onNodeWithTag("StatusFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("StatusOption_Completed").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Stay productive! Start by adding a new task.").assertExists()

        composeTestRule.onNodeWithTag("StatusFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("StatusOption_Pending").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Web App").assertExists()

        composeTestRule.onNodeWithTag("StatusFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("StatusOption_In Progress").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Stay productive! Start by adding a new task.").assertExists()
        composeTestRule.onNodeWithTag("StatusFiltering").performClick()
        composeTestRule.onNodeWithTag("StatusOption_All").performClick()

        // Sort Filtering

        composeTestRule.onNodeWithTag("SortFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SortOption_Latest").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("TaskItem_0")
            .assertTextContains("Mobile App")

        composeTestRule.onNodeWithTag("SortFiltering").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("SortOption_Oldest").performClick()
        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("TaskItem_0")
            .assertTextContains("SEO")

    }


}
