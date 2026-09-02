package com.example.fitswap.ui.screens.exercises

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ExerciseCatalogFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun buscarUnEjercicioYAbrirSuHistorialMuestraElVolumenAcumulado() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_exercises").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Ejercicios")

        composeTestRule.onNodeWithTag("exerciseSearchField").performTextInput("press de pecho")
        composeTestRule.onNodeWithTag("exerciseItem_press-de-pecho").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Historial — Press de pecho")
        composeTestRule.onNodeWithTag("maxWeightLabel").assertTextEquals("Peso máximo: 60.0 kg")
    }
}
