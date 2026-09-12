package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import android.Manifest
import androidx.test.rule.GrantPermissionRule
import com.example.fitswap.MainActivity
import com.example.fitswap.navigation.Destination
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

    // La pantalla de ejercicio activo pide POST_NOTIFICATIONS al abrirse (timer en foreground
    // service) — sin otorgarlo de antemano, el diálogo del sistema tapa la app y Compose deja de
    // encontrar la jerarquía.
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun cierraElLoopCompletoDeEntrenamientoDePuntaAPunta() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        // El día arranca en el calentamiento de cardio (rutina sembrada), no en fuerza.
        composeTestRule.waitUntilTagExists("cardioStartButton")
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elíptica")

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

        // Terminar rutina guardando lo registrado vuelve a Rutinas.
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.waitUntilTagExists("finishRoutineItem")
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithTag("saveRoutineButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
    }

    @Test
    fun cancelarTerminarRutinaMantieneElMenuDeSesion() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.waitUntilTagExists("cardioStartButton")

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.waitUntilTagExists("finishRoutineItem")
        composeTestRule.onNodeWithTag("finishRoutineItem").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        // Se mantiene en el menú de sesión, con el día todavía visible.
        composeTestRule.onNodeWithTag("sessionMenuItem_default-martes-press-militar-maquina").assertTextEquals(
            "○  Press militar en máquina"
        )
    }

    @Test
    fun elMenuDeSesionSeAbreYCierraConGestoDeSwipe() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.waitUntilTagExists("cardioStartButton")

        composeTestRule.onRoot().performTouchInput { swipeRight() }
        composeTestRule.waitUntilTagExists("sessionMenuItem_default-martes-press-militar-maquina")

        composeTestRule.onRoot().performTouchInput { swipeLeft() }
        composeTestRule.waitUntilTagExists("cardioStartButton")
    }

    @Test
    fun swipeDuranteNotasNoAbreElDrawerDeNavegacion() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.waitUntilTagExists("cardioStartButton")

        composeTestRule.onNodeWithTag("notesButton").performClick()
        composeTestRule.waitUntilTagExists("noteInputField")

        composeTestRule.onRoot().performTouchInput { swipeRight() }
        composeTestRule.onNodeWithTag("drawerItem_${Destination.Routines.route}").assertDoesNotExist()
    }
}
