package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.ComingSoonScreen
import com.example.fitswap.ui.screens.routines.RoutineDetailScreen
import com.example.fitswap.ui.screens.routines.RoutineListScreen

private const val ROUTINE_ID_ARG = "routineId"
private const val NEW_ROUTINE_ID = "new"

private fun routineDetailRoute(routineId: String) = "routineDetail/$routineId"
private fun editRoutineRoute(routineId: String) = "editRoutine/$routineId"

fun NavGraphBuilder.routineListScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Routines.route) {
        RoutineListScreen(
            onMenuClick = onMenuClick,
            onRoutineClick = { routineId -> navController.navigate(routineDetailRoute(routineId)) },
            onNewRoutineClick = { navController.navigate(editRoutineRoute(NEW_ROUTINE_ID)) }
        )
    }
}

fun NavGraphBuilder.routineDetailScreen(navController: NavHostController) {
    composable(
        route = "routineDetail/{$ROUTINE_ID_ARG}",
        arguments = listOf(navArgument(ROUTINE_ID_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val routineId = backStackEntry.arguments?.getString(ROUTINE_ID_ARG).orEmpty()
        RoutineDetailScreen(
            onBack = { navController.popBackStack() },
            onStartWorkout = { navController.navigate("activeExercise") },
            onEditRoutine = { navController.navigate(editRoutineRoute(routineId)) },
        )
    }
}

fun NavGraphBuilder.editRoutineStubScreen(navController: NavHostController) {
    composable(
        route = "editRoutine/{$ROUTINE_ID_ARG}",
        arguments = listOf(navArgument(ROUTINE_ID_ARG) { type = NavType.StringType })
    ) {
        ComingSoonScreen(
            title = "Editar rutina",
            navigationIcon = { BackNavigationIcon(onBack = { navController.popBackStack() }) }
        )
    }
}

fun NavGraphBuilder.activeExerciseStubScreen(navController: NavHostController) {
    composable("activeExercise") {
        ComingSoonScreen(
            title = "Ejercicio activo",
            navigationIcon = { BackNavigationIcon(onBack = { navController.popBackStack() }) }
        )
    }
}
