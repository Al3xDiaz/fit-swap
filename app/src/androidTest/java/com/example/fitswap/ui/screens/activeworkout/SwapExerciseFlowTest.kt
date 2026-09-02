package com.example.fitswap.ui.screens.activeworkout

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun elegirUnSustitutoReemplazaElEjercicioEnCursoSinSalirDeLaPantalla() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()
        // Por defecto se abre el día de hoy (fijado a martes/Push en los tests, ver TestDateModule) —
        // cambiamos a Jueves/Legs con el selector de día.
        composeTestRule.onNodeWithTag("changeDayButton").performClick()
        composeTestRule.onNodeWithTag("dayPickerItem_default-jueves").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-jueves").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Sentadilla / hack squat / prensa")

        composeTestRule.onNodeWithTag("swapExerciseButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle")
            .assertTextEquals("Alternativas a \"Sentadilla / hack squat / prensa\"")

        composeTestRule.onNodeWithTag("substituteItem_prensa-sentadilla-ligera").performClick()

        // Vuelve a Ejercicio activo con el sustituto elegido, sin perder el progreso de la rutina.
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Prensa / sentadilla ligera")
    }
}
