package com.example.taskmanager

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.taskmanager.ui.components.BouncyFAB
import com.example.taskmanager.ui.screen.AddTaskScreen
import com.example.taskmanager.ui.screen.TaskListScreen
import com.example.taskmanager.viewModel.FakeTaskViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BouncyFABTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeTaskViewModel
    private var scaleState: Animatable<Float, AnimationVector1D>? = null

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        fakeViewModel = FakeTaskViewModel(application)
    }

    @Test
    fun bouncyFAB_AnimationChangesScaleValue() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            scaleState = remember { Animatable(1f) }  // Store scale state

            // Set up a navigation graph
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

            // Add FAB separately for testing
            BouncyFAB(navController, scaleState!!)
        }

        // Ensure we are selecting only one FAB
        val fabNode = composeTestRule.onAllNodesWithTag("AddTask").onFirst()

        // Verify initial scale is 1f
        composeTestRule.runOnIdle {
            assertEquals(1f, scaleState!!.value, 0.001f)
        }

        // Perform click
        fabNode.performClick()

        // Simulate animation time passing
        composeTestRule.mainClock.advanceTimeBy(350) // Total duration of animation

        // Allow animations to play
        composeTestRule.waitForIdle()

        // Verify scale value changed (Animation triggered)
        composeTestRule.runOnIdle {
            assertNotEquals(1f, scaleState!!.value)
        }
    }


}
