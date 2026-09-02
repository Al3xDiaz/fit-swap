package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitswap.ui.screens.summary.SummaryScreen

fun NavGraphBuilder.summaryScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Summary.route) {
        SummaryScreen(
            onMenuClick = onMenuClick,
            onViewExerciseHistory = { navController.navigate(Destination.Exercises.route) },
        )
    }
}
