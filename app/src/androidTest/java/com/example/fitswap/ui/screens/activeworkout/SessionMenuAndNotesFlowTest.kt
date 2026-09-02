package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SessionMenuAndNotesFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun cierraElLoopCompletoDeEntrenamientoDePuntaAPunta() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")

        // Menú de sesión: saltar a otro ejercicio del día sin perder el progreso.
        composeTestRule.onNodeWithTag("sessionMenuButton").performClick()
        composeTestRule.onNodeWithTag("sessionMenuItem_default-martes-press-militar-maquina").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        // Notas: agregar una nota sin perder el contexto del entrenamiento.
        composeTestRule.onNodeWithTag("notesButton").performClick()
        composeTestRule.onNodeWithTag("noteInputField").performTextInput("Ajustar asiento")
        composeTestRule.onNodeWithTag("addNoteButton").performClick()
        composeTestRule.onNodeWithText("Ajustar asiento").assertTextEquals("Ajustar asiento")
        composeTestRule.onNodeWithContentDescription("Cerrar").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        // Terminar rutina (con confirmación) vuelve a Rutinas.
        composeTestRule.onNodeWithTag("sessionMenuButton").performClick()
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
    }

    @Test
    fun cancelarTerminarRutinaMantieneElMenuDeSesion() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()

        composeTestRule.onNodeWithTag("sessionMenuButton").performClick()
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        // Se mantiene en el menú de sesión, con el día todavía visible.
        composeTestRule.onNodeWithTag("sessionMenuItem_default-martes-press-militar-maquina").assertTextEquals(
            "○  Press militar en máquina"
        )
    }
}
