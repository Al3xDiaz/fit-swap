package com.example.fitswap.ui.screens.settings

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import android.Manifest
import androidx.compose.ui.test.performScrollTo
import androidx.test.rule.GrantPermissionRule
import com.example.fitswap.MainActivity
import com.example.fitswap.TimerServicesCleanupRule
import com.example.fitswap.waitUntilTagExists
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // La pantalla de ejercicio activo pide POST_NOTIFICATIONS al abrirse (timer en foreground
    // service) — sin otorgarlo de antemano, el diálogo del sistema tapa la app y Compose deja de
    // encontrar la jerarquía.
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    // El rest timer de este flujo es un foreground service real que no se detiene solo si el test
    // termina antes de que llegue a cero — sin esto, sigue publicando estado (y contendiendo CPU)
    // durante los tests siguientes de la suite (ver TimerServicesCleanupRule).
    @get:Rule(order = 0)
    val timerCleanupRule = TimerServicesCleanupRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun cambiarElTemaAOscuroOscureceLaPantalla() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_settings").performClick()

        // Configuraciones ahora scrollea (Perfil + Paleta de colores se agregaron arriba de Tema).
        composeTestRule.onNodeWithTag("themeOption_LIGHT").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val lightLuminance = composeTestRule.onRoot().captureToImage().averageLuminance()

        composeTestRule.onNodeWithTag("themeOption_DARK").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val darkLuminance = composeTestRule.onRoot().captureToImage().averageLuminance()

        assertTrue(
            "esperaba que el tema oscuro sea más oscuro que el claro (claro=$lightLuminance, oscuro=$darkLuminance)",
            darkLuminance < lightLuminance
        )
    }

    @Test
    fun elegirUnTimerDeDescansoEnConfiguracionesSeUsaAlRegistrarUnaSerie() {
        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_settings").performClick()
        composeTestRule.onNodeWithTag("restTimerOption_30").performScrollTo().performClick()

        composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
        composeTestRule.onNodeWithTag("drawerItem_routines").performClick()
        composeTestRule.onNodeWithTag("startWorkoutButton_default-martes").performClick()

        // El día ahora arranca en el calentamiento de cardio (rutina sembrada) — saltarlo para
        // llegar a un ejercicio de fuerza donde registrar una serie.
        composeTestRule.onNodeWithTag("nextExerciseButton").performClick()
        composeTestRule.onNodeWithTag("registerSetButton").performClick()
        composeTestRule.waitUntilTagExists("restTimerLabel")
        composeTestRule.onNodeWithTag("restTimerLabel").assertTextEquals("⏱ Descanso: 00:30")
    }
}

private fun ImageBitmap.averageLuminance(): Double {
    val pixelMap = toPixelMap()
    val stepX = (pixelMap.width / 50).coerceAtLeast(1)
    val stepY = (pixelMap.height / 50).coerceAtLeast(1)
    var total = 0.0
    var count = 0
    var x = 0
    while (x < pixelMap.width) {
        var y = 0
        while (y < pixelMap.height) {
            val color = pixelMap[x, y]
            total += (color.red + color.green + color.blue) / 3.0
            count++
            y += stepY
        }
        x += stepX
    }
    return total / count
}
