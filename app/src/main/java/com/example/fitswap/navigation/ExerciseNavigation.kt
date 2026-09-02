package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitswap.ui.screens.exercises.ExerciseCatalogScreen
import com.example.fitswap.ui.screens.exercises.ExerciseHistoryScreen

private const val CATALOG_EXERCISE_ID_ARG = "exerciseId"

private fun exerciseHistoryRoute(exerciseId: String) = "exerciseHistory/$exerciseId"

fun NavGraphBuilder.exerciseCatalogScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Exercises.route) {
        ExerciseCatalogScreen(
            onMenuClick = onMenuClick,
            onExerciseClick = { exerciseId -> navController.navigate(exerciseHistoryRoute(exerciseId)) },
        )
    }
}

fun NavGraphBuilder.exerciseHistoryScreen(navController: NavHostController) {
    composable(
        route = "exerciseHistory/{$CATALOG_EXERCISE_ID_ARG}",
        arguments = listOf(navArgument(CATALOG_EXERCISE_ID_ARG) { type = NavType.StringType })
    ) {
        ExerciseHistoryScreen(onBack = { navController.popBackStack() })
    }
}
