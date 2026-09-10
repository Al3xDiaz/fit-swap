package com.example.fitswap.ui.screens.measurements

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import com.example.fitswap.MainActivity
import com.example.fitswap.waitUntilTagExists
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class BodyMeasurementsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun agregarUnaMedicionCompletaAparecEnElHistorialConSuImcYPorcentajeDeGrasa() {
        // Sexo y altura se configuran una sola vez en Configuración > Perfil, no en cada medición.
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_settings").performClick()
        composeTestRule.onNodeWithTag("sexOption_MALE").performClick()
        composeTestRule.onNodeWithTag("heightField").performTextInput("180")
        closeSoftKeyboard()

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_measurements").performClick()
        composeTestRule.onAllNodesWithTag("profileIncompleteHint").assertCountEquals(0)

        composeTestRule.onNodeWithTag("addMeasurementButton").performClick()
        // Paso 1 del stepper: elegir qué circunferencias se van a medir antes de ver sus campos.
        composeTestRule.onNodeWithTag("fieldOption_neck").performClick()
        composeTestRule.onNodeWithTag("fieldOption_waist").performClick()
        composeTestRule.onNodeWithTag("nextStepButton").performClick()

        composeTestRule.onNodeWithTag("weightField").performTextInput("81")
        composeTestRule.onNodeWithTag("neckField").performTextInput("38")
        composeTestRule.onNodeWithTag("waistField").performTextInput("85")
        closeSoftKeyboard()
        // `performClick()` no siempre auto-scrollea un `Modifier.verticalScroll()` plano (a
        // diferencia de LazyColumn) — hay que llevar el botón a la vista explícitamente.
        composeTestRule.onNodeWithTag("saveMeasurementButton").performScrollTo().performClick()
        // Guardar navega de vuelta a Medidas — esperar a que la transición asiente antes de leer
        // el historial actualizado (mismo motivo que M11: hay una vuelta async entre el click y
        // que el nuevo estado esté realmente en pantalla).
        composeTestRule.waitUntilTagExists("addMeasurementButton")

        // 81 kg / 1.80 m^2 = 25.0 -> categoría "sobrepeso" (el corte OMS de "normal" es < 25.0).
        composeTestRule.onNodeWithText("IMC: 25.0 (sobrepeso)").assertIsDisplayed()
    }

    @Test
    fun abrirLaGuiaDeMedicionMuestraLosItemsNuevosYVolverRegresaAlFormulario() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_measurements").performClick()
        composeTestRule.onNodeWithTag("addMeasurementButton").performClick()

        composeTestRule.onNodeWithTag("measurementGuideButton").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Cómo medir")
        composeTestRule.onNodeWithTag("measurementGuideItem_wrist").assertIsDisplayed()
        composeTestRule.onNodeWithTag("measurementGuideItem_glute").assertIsDisplayed()
        composeTestRule.onNodeWithTag("measurementGuideItem_forearm").assertIsDisplayed()
        composeTestRule.onNodeWithTag("measurementGuideItem_shoulder").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Nueva medición")
        composeTestRule.onNodeWithTag("nextStepButton").assertIsDisplayed()
    }

    @Test
    fun elStepperSoloMuestraLosCamposElegidosYDeseleccionarUnoNoLoGuarda() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_measurements").performClick()
        composeTestRule.onNodeWithTag("addMeasurementButton").performClick()

        composeTestRule.onNodeWithTag("fieldOption_neck").performClick()
        composeTestRule.onNodeWithTag("fieldOption_waist").performClick()
        composeTestRule.onNodeWithTag("nextStepButton").performClick()

        // Solo aparecen los campos elegidos, no las otras 9 circunferencias.
        composeTestRule.onNodeWithTag("neckField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("waistField").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("hipField").assertCountEquals(0)

        // Volver atrás, deseleccionar cintura y confirmar que no se guarda con la medición.
        composeTestRule.onNodeWithTag("backToFieldSelectionButton").performClick()
        composeTestRule.onNodeWithTag("fieldOption_waist").performClick()
        composeTestRule.onNodeWithTag("nextStepButton").performClick()
        composeTestRule.onAllNodesWithTag("waistField").assertCountEquals(0)

        composeTestRule.onNodeWithTag("weightField").performTextInput("70")
        closeSoftKeyboard()
        composeTestRule.onNodeWithTag("saveMeasurementButton").performScrollTo().performClick()
        composeTestRule.waitUntilTagExists("addMeasurementButton")
    }

    @Test
    fun editarUnaMedicionYaListadaActualizaSuImc() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_settings").performClick()
        composeTestRule.onNodeWithTag("sexOption_MALE").performClick()
        composeTestRule.onNodeWithTag("heightField").performTextInput("180")
        closeSoftKeyboard()

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_measurements").performClick()
        // "seed-2" es la medición sembrada más reciente (76.5 kg) — con 1.80 m da IMC normal.
        composeTestRule.onNodeWithTag("bmiLabel_seed-2").assertTextEquals("IMC: 23.6 (normal)")

        composeTestRule.onNodeWithTag("editMeasurementButton_seed-2").performClick()
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("Editar medición")
        composeTestRule.onNodeWithTag("editWeightField").performTextClearance()
        composeTestRule.onNodeWithTag("editWeightField").performTextInput("82")
        closeSoftKeyboard()
        composeTestRule.onNodeWithTag("saveMeasurementEditButton").performScrollTo().performClick()

        // Vuelve a Medidas — el mismo id refleja el peso nuevo, sin duplicar la fila.
        composeTestRule.waitUntilTagExists("bmiLabel_seed-2")
        composeTestRule.onNodeWithTag("bmiLabel_seed-2").assertTextEquals("IMC: 25.3 (sobrepeso)")
    }
}
