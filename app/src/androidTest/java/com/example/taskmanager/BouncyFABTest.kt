package com.example.taskmanager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.ui.tasks.components.BouncyFAB
import com.example.taskmanager.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class BouncyFABTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var scaleState: Animatable<Float, AnimationVector1D>? = null

    @Test
    fun bouncyFAB_AnimationChangesScaleValue() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            val navController = rememberNavController()
            scaleState = remember { Animatable(1f) }
            NavHost(navController = navController, startDestination = Screen.TaskList.route) {
                composable(Screen.TaskList.route) { BouncyFAB(navController, scaleState!!) }
                composable(Screen.AddTask.route) {}
            }
        }

        composeTestRule.runOnIdle { assertEquals(1f, scaleState!!.value, 0.001f) }

        composeTestRule.onNodeWithTag("AddTask").performClick()
        // mid-animation, the FAB should be squashed
        composeTestRule.mainClock.advanceTimeBy(60)

        composeTestRule.runOnIdle { assertNotEquals(1f, scaleState!!.value) }
    }
}
