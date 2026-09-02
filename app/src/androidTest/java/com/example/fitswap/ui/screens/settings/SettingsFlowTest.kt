package com.example.fitswap.ui.screens.settings

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.fitswap.MainActivity
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

        composeTestRule.onNodeWithTag("themeOption_LIGHT").performClick()
        composeTestRule.waitForIdle()
        val lightLuminance = composeTestRule.onRoot().captureToImage().averageLuminance()

        composeTestRule.onNodeWithTag("themeOption_DARK").performClick()
        composeTestRule.waitForIdle()
        val darkLuminance = composeTestRule.onRoot().captureToImage().averageLuminance()

        assertTrue(
            "esperaba que el tema oscuro sea más oscuro que el claro (claro=$lightLuminance, oscuro=$darkLuminance)",
            darkLuminance < lightLuminance
        )
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
