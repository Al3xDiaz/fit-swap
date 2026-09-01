package com.example.fitswap.navigation

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.fitswap.ui.theme.FitSwapTheme
import org.junit.Rule
import org.junit.Test

class AppNavHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun drawerAbreYNavegaACadaSeccionDeNivelSuperior() {
        composeTestRule.setContent {
            FitSwapTheme {
                AppNavHost()
            }
        }

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals(Destination.Routines.label)

        topLevelDestinations
            .filter { it != Destination.Routines }
            .forEach { destination ->
                composeTestRule.onNodeWithContentDescription("Abrir menú").performClick()
                composeTestRule.onNodeWithTag("drawerItem_${destination.route}").performClick()
                composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals(destination.label)
            }
    }
}
