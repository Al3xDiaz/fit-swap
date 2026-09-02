package com.example.fitswap.navigation

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.fitswap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppNavHostTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun drawerAbreYNavegaACadaSeccionDeNivelSuperior() {
        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals(Destination.Routines.label)

        topLevelDestinations
            .filter { it != Destination.Routines }
            .forEach { destination ->
                composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
                composeTestRule.onNodeWithTag("drawerItem_${destination.route}").performClick()
                composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals(expectedTopBarTitle(destination))
            }
    }

    // Resumen/Reporte usa "Resumen" como título de pantalla (boceto de docs/DIAGRAMS.md); el resto
    // de las secciones todavía no implementadas siguen mostrando su label completo vía ComingSoonScreen.
    private fun expectedTopBarTitle(destination: Destination): String =
        if (destination == Destination.Summary) "Resumen" else destination.label
}
