package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.rule.GrantPermissionRule
import com.example.fitswap.MainActivity
import com.example.fitswap.waitUntilTagExists
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ActiveExerciseFlowTest {

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
    fun registrarUnaSerieArrancaElTimerYAvanzaElContador() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()

        // El día ahora arranca con un calentamiento de cardio (rutina sembrada) — saltarlo para
        // llegar al primer ejercicio de fuerza.
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elíptica")
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Calentamiento")

        // Registrar el calentamiento arranca el temporizador de descanso y avanza a la primera serie efectiva.
        // El descanso corre en un foreground service (M15) — esperar el viaje async antes de leerlo.
        composeTestRule.onNodeWithTag("registerSetButton").performClick()
        composeTestRule.waitUntilTagExists("restTimerLabel")
        composeTestRule.onNodeWithTag("restTimerLabel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Serie 1 de 4 (efectiva)")

        // Registrar la primera serie efectiva avanza el contador de series efectivas.
        composeTestRule.onNodeWithTag("registerSetButton").performClick()
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Serie 2 de 4 (efectiva)")
    }

    @Test
    fun elBotonSiguienteAvanzaDeEjercicioSinTerminarElActual() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        // El día arranca en el calentamiento de cardio (rutina sembrada).
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elíptica")

        // "Siguiente" está siempre disponible, no requiere terminar el ejercicio actual.
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")

        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Press militar en máquina")

        composeTestRule.onNodeWithTag("previousExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")
    }

    @Test
    fun volverDesdeEjercicioActivoPideConfirmacionYTerminaLaRutina() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        // El calentamiento del día es cardio, no fuerza — esperar su botón de inicio en vez de
        // "registerSetButton" (que solo existe en la pestaña de fuerza).
        composeTestRule.waitUntilTagExists("cardioStartButton")

        // El ícono de la topBar ahora abre el menú de sesión (reemplaza al drawer principal
        // mientras hay rutina activa, ver AppNavHost) — salir se hace con el back del sistema, que
        // ahora ofrece el modal Guardar/Descartar/Cancelar en vez de solo confirmar.
        Espresso.pressBack()
        composeTestRule.onNodeWithTag("discardRoutineButton").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")
    }

    @Test
    fun completarUnEjercicioYLuegoDescartarSusDatosLoDejaComoAntesDeEmpezarlo() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick() // saltar el calentamiento
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")

        repeat(5) { composeTestRule.onNodeWithTag("registerSetButton").performClick() } // 1 warmup + 4 effective
        composeTestRule.onNodeWithTag("exerciseCompleteLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("discardExerciseDataButton").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()

        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Calentamiento")
    }

    @Test
    fun descartarLaRutinaPorElBackBorraElProgresoYNoQuedaGuardadoAlReabrir() {
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick() // saltar el calentamiento
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")

        composeTestRule.onNodeWithTag("registerSetButton").performClick() // progreso parcial, nunca se completa
        Espresso.pressBack()
        composeTestRule.onNodeWithTag("discardRoutineButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Rutinas")

        // Reabrir la misma rutina: el ejercicio no debe conservar la serie descartada.
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Elevaciones laterales")
        composeTestRule.onNodeWithTag("currentStageLabel").assertTextEquals("Calentamiento")
    }
}
