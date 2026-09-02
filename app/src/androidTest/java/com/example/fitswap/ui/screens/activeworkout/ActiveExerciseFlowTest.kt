package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ActiveExerciseFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun registrarUnaSerieArrancaElTimerYAvanzaElContador() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Calentamiento")

        // Registrar el calentamiento arranca el temporizador de descanso y avanza a la primera serie efectiva.
        composeTestRule.onNodeWithTag("registerSetButton").performClick()
        composeTestRule.onNodeWithTag("restTimerLabel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Serie 1 de 4 (efectiva)")

        // Registrar la primera serie efectiva avanza el contador de series efectivas.
        composeTestRule.onNodeWithTag("registerSetButton").performClick()
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Serie 2 de 4 (efectiva)")
    }
}
