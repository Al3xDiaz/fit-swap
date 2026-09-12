package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import android.Manifest
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SwapExerciseFlowTest {

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
    fun elegirUnSustitutoReemplazaElEjercicioEnCursoSinSalirDeLaPantalla() {
        // Por defecto se abre el día de hoy (fijado a martes/Push en los tests, ver TestDateModule) —
        // cambiamos a Jueves/Legs con el selector de día.
        composeTestRule.onNodeWithTag("changeDayButton").performClick()
        composeTestRule.onNodeWithTag("weekDayPickerItem_THURSDAY").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-jueves").performClick()

        // El día ahora arranca en el calentamiento de cardio (rutina sembrada) — saltarlo.
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Sentadilla / hack squat / prensa")

        composeTestRule.onNodeWithTag("swapExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle")
            .assertTextEquals("Alternativas a \"Sentadilla / hack squat / prensa\"")

        composeTestRule.onNodeWithTag("substituteItem_prensa-sentadilla-ligera").performClick()

        // Vuelve a Ejercicio activo con el sustituto elegido, sin perder el progreso de la rutina.
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Prensa / sentadilla ligera")
    }
}
