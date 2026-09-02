package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fitswap.ui.screens.settings.SettingsScreen

fun NavGraphBuilder.settingsScreen(onMenuClick: () -> Unit) {
    composable(Destination.Settings.route) {
        SettingsScreen(onMenuClick = onMenuClick)
    }
}
