package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.fitswap.ui.screens.tools.ToolsScreen

fun NavGraphBuilder.toolsScreen(onMenuClick: () -> Unit) {
    composable(Destination.Tools.route) {
        ToolsScreen(onMenuClick = onMenuClick)
    }
}
