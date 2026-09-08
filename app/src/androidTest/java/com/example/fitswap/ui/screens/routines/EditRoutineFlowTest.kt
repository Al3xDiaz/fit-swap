package com.example.fitswap.ui.screens.routines

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

@HiltAndroidTest
class EditRoutineFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun crearUnaRutinaAgregarUnDiaYUnEjercicioYLuegoEliminarloTodo() {
        // Crear rutina nueva desde Rutinas.
        composeTestRule.onNodeWithTag("newRoutineButton").performClick()
        composeTestRule.onNodeWithTag("newRoutineNameField").performTextInput("Rutina de prueba")
        composeTestRule.onNodeWithTag("createRoutineButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutina de prueba")

        // Agregar un día.
        composeTestRule.onNodeWithTag("newDayOfWeekField").performClick()
        composeTestRule.onNodeWithTag("dayOfWeekOption_MONDAY").performClick()
        composeTestRule.onNodeWithTag("newDayNameField").performTextInput("Día A")
        composeTestRule.onNodeWithTag("addDayButton").performClick()
        composeTestRule.onNodeWithTag("dayName_routine-0-dia-0").assertTextEquals("Lunes — Día A")

        // Agregar un ejercicio desde el catálogo (modo elegir).
        composeTestRule.onNodeWithTag("addExerciseButton_routine-0-dia-0").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Ejercicios")
        composeTestRule.onNodeWithTag("exerciseItem_caminar-en-cinta").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutina de prueba")
        composeTestRule.onAllNodesWithTag("editExerciseRow_routine-0-dia-0-caminar-en-cinta").assertCountEquals(1)

        // Renombrar el día.
        composeTestRule.onNodeWithTag("renameDayButton_routine-0-dia-0").performClick()
        composeTestRule.onNodeWithTag("dayNameField_routine-0-dia-0").performTextClearance()
        composeTestRule.onNodeWithTag("dayNameField_routine-0-dia-0").performTextInput("Día renombrado")
        composeTestRule.onNodeWithTag("confirmRenameDayButton_routine-0-dia-0").performClick()
        composeTestRule.onNodeWithTag("dayName_routine-0-dia-0").assertTextEquals("Lunes — Día renombrado")

        // Quitar el ejercicio agregado.
        composeTestRule.onNodeWithTag("removeExerciseButton_routine-0-dia-0-caminar-en-cinta").performClick()
        composeTestRule.onAllNodesWithTag("editExerciseRow_routine-0-dia-0-caminar-en-cinta").assertCountEquals(0)

        // Quitar el día.
        composeTestRule.onNodeWithTag("removeDayButton_routine-0-dia-0").performClick()
        composeTestRule.onAllNodesWithTag("dayName_routine-0-dia-0").assertCountEquals(0)

        // Eliminar la rutina completa vuelve a Rutinas y ya no aparece en la lista.
        composeTestRule.onNodeWithTag("deleteRoutineButton").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
        composeTestRule.onAllNodesWithTag("routineListItem_routine-0").assertCountEquals(0)
    }

    @Test
    fun eliminarUnaRutinaDesdeEditarLlegandoPorDetalleVuelveDirectoARutinas() {
        // Regresión: llegar a Editar rutina vía Detalle y eliminar ahí no debe dejar a Detalle
        // (backstack de abajo) observando una rutina que ya no existe — ver RoutineNavigation.kt.
        composeTestRule.onNodeWithTag("routineListItem_full-body-rapido").performClick()
        composeTestRule.onNodeWithContentDescription("Editar rutina").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Full Body Rápido")

        composeTestRule.onNodeWithTag("deleteRoutineButton").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
        composeTestRule.onAllNodesWithTag("routineListItem_full-body-rapido").assertCountEquals(0)
    }
}
