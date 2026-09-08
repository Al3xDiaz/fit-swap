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
        composeTestRule.onNodeWithTag("weightField").assertIsDisplayed()
    }
}
