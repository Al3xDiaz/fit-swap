package com.example.fitswap.ui.screens.exercises

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.fitswap.MainActivity
import com.example.fitswap.waitUntilTagExists
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Id determinista de la primera sesión de cardio completada para un ejercicio de cardio fresco
 * (ver `ActiveExerciseViewModel.completeCardioSession`: `"${exerciseId}-${loggedSetSeq++}"`, y
 * `loggedSetSeq` arranca en 0 para cardio). El calentamiento del día Push es "eliptica". */
private const val CARDIO_SESSION_ID = "eliptica-0"

@HiltAndroidTest
class CardioHistoryFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun editarYLuegoEliminarUnaSesionDeCardioDesdeElHistorial() {
        // Completar una sesión real de cardio desde el calentamiento del día Push.
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.waitUntilTagExists("cardioStartButton")
        composeTestRule.onNodeWithTag("cardioStartButton").performClick()
        Thread.sleep(1_100) // el timer corre en tiempo real (foreground service) — necesita >0s transcurridos.
        composeTestRule.onNodeWithTag("cardioStopButton").performClick()
        composeTestRule.onNodeWithTag("cardioDistanceField").performTextInput("3")
        composeTestRule.onNodeWithTag("completeCardioSessionButton").performClick()

        // Ir al historial del ejercicio desde el catálogo.
        composeTestRule.waitUntilTagExists("registerSetButton") // avanzó al siguiente ejercicio (fuerza)
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithText("Terminar rutina").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_exercises").performClick()
        composeTestRule.onNodeWithTag("exerciseSearchField").performTextInput("Elíptica")
        composeTestRule.onNodeWithTag("exerciseItem_eliptica").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Historial — Elíptica")

        // Editar la sesión: cambia la distancia.
        composeTestRule.onNodeWithTag("editCardioButton_$CARDIO_SESSION_ID").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Editar sesión — Elíptica")
        composeTestRule.onNodeWithTag("editCardioDistanceField").performTextClearance()
        composeTestRule.onNodeWithTag("editCardioDistanceField").performTextInput("4.5")
        composeTestRule.onNodeWithTag("saveCardioSessionButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Historial — Elíptica")

        // Eliminar la sesión — no queda ninguna, así que vuelve al mensaje de "sin historial".
        composeTestRule.onNodeWithTag("deleteCardioButton_$CARDIO_SESSION_ID").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithTag("noHistoryLabel").assertTextEquals("Sin historial todavía")
    }
}
