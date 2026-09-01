package com.example.fitswap.ui.screens.routines

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.fitswap.MainActivity
import com.example.fitswap.navigation.Destination
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RoutineFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun tocarUnaRutinaAbreSuDetalle() {
        composeTestRule.onNodeWithTag("routineListItem_default").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals("PPL + Upper/Accesorios")
    }

    @Test
    fun eliminarConfirmadoQuitaLaRutinaNoDefaultDeLaLista() {
        composeTestRule.onNodeWithTag("routineListItem_full-body-rapido").performClick()
        composeTestRule.onNodeWithTag("deleteRoutineButton").performClick()
        composeTestRule.onNodeWithText("Confirmar").performClick()

        composeTestRule.onNodeWithTag("appTopBarTitle").assertTextEquals(Destination.Routines.label)
        composeTestRule.onAllNodesWithTag("routineListItem_full-body-rapido").assertCountEquals(0)
    }
}
