package com.example.fitswap.ui.screens.exercises

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Ids deterministas del historial sembrado de "press-de-pecho" (ver `buildHistorySeed`): la
 * sesión del 2026-08-18 tiene 1 serie de aproximación + 4 efectivas, y es la de peso máximo. */
private const val FIRST_EFFECTIVE_ROW_ID = "press-de-pecho-2026-08-18-EFFECTIVE-1"

@HiltAndroidTest
class HistorySessionDetailFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun editarUnaSerieDelHistorialActualizaElPesoMaximoYLuegoEliminarElRegistroLoQuitaDelTodo() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_exercises").performClick()
        composeTestRule.onNodeWithTag("exerciseSearchField").performTextInput("press de pecho")
        composeTestRule.onNodeWithTag("exerciseItem_press-de-pecho").performClick()
        composeTestRule.onNodeWithTag("maxWeightLabel").assertTextEquals("Peso máximo: 60.0 kg")

        // Editar una serie efectiva del registro de peso máximo.
        composeTestRule.onNodeWithTag("editHistoryButton_2026-08-18").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press de pecho — 2026-08-18")
        composeTestRule.onNodeWithTag("historyDetailWeightField_$FIRST_EFFECTIVE_ROW_ID").performTextClearance()
        composeTestRule.onNodeWithTag("historyDetailWeightField_$FIRST_EFFECTIVE_ROW_ID").performTextInput("65")
        composeTestRule.onNodeWithTag("saveHistoryDetailButton").performClick()
        composeTestRule.onNodeWithTag("maxWeightLabel").assertTextEquals("Peso máximo: 65.0 kg")

        // Eliminar el registro completo: vuelve al peso máximo de la sesión anterior (57.5 kg).
        composeTestRule.onNodeWithTag("deleteHistoryButton_2026-08-18").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithTag("maxWeightLabel").assertTextEquals("Peso máximo: 57.5 kg")
        composeTestRule.onAllNodesWithTag("historySessionRow_2026-08-18").assertCountEquals(0)
    }
}
