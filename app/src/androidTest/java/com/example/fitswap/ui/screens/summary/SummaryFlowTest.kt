package com.example.fitswap.ui.screens.summary

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SummaryFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun muestraMetricasCalculadasYNavegaAlCatalogoDeEjercicios() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_summary").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Resumen")
        composeTestRule.onNodeWithTag("completedWorkoutsLabel").assertTextEquals(
            "Entrenamientos completados (este mes): 0"
        )
        // press-de-pecho (6420 kg) + sentadilla-hack-prensa (6800 kg) sembrados en agosto 2026 en FakeHistoryRepository.
        composeTestRule.onNodeWithTag("monthlyVolume_2026-08").assertTextEquals("Ago 2026: 13220.0 kg")

        composeTestRule.onNodeWithTag("viewExerciseHistoryButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Ejercicios")
    }
}
