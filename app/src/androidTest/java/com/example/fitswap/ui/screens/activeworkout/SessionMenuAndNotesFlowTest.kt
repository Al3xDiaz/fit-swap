package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.fitswap.MainActivity
import com.example.fitswap.waitUntilTagExists
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
        composeTestRule.waitUntilTagExists("registerSetButton")
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")

        // Menú de sesión: ahora reemplaza al drawer principal mientras hay una rutina activa —
        // se abre con el mismo ícono/posición que el menú de la app (ver AppNavHost).
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.waitUntilTagExists("sessionMenuItem_default-martes-press-militar-maquina")
        composeTestRule.onNodeWithTag("sessionMenuItem_default-martes-press-militar-maquina").performClick()
        composeTestRule.waitUntilTagExists("registerSetButton")
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        // Notas: escribir la nota de hoy sin perder el contexto del entrenamiento — una sola
        // nota por día, se guarda al cerrar.
        composeTestRule.onNodeWithTag("notesButton").performClick()
        composeTestRule.waitUntilTagExists("noteInputField")
        composeTestRule.onNodeWithTag("noteInputField").performTextInput("Ajustar asiento")
        composeTestRule.onNodeWithTag("saveNoteButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        // Reabrir Notas confirma que quedó guardada.
        composeTestRule.onNodeWithTag("notesButton").performClick()
        composeTestRule.waitUntilTagExists("noteInputField")
        composeTestRule.onNodeWithTag("noteInputField").assertTextEquals("Ajustar asiento")
        composeTestRule.onNodeWithContentDescription("Cerrar").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        // Terminar rutina (con confirmación) vuelve a Rutinas.
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.waitUntilTagExists("finishRoutineItem")
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
    }

    @Test
    fun cancelarTerminarRutinaMantieneElMenuDeSesion() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.waitUntilTagExists("registerSetButton")

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.waitUntilTagExists("finishRoutineItem")
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        // Se mantiene en el menú de sesión, con el día todavía visible.
        composeTestRule.onNodeWithTag("sessionMenuItem_default-martes-press-militar-maquina").assertTextEquals(
            "○  Press militar en máquina"
        )
    }
}
