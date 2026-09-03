package com.example.fitswap.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.ComingSoonScreen
import com.example.fitswap.ui.screens.activeworkout.ActiveExerciseScreen
import com.example.fitswap.ui.screens.activeworkout.NotesScreen
import com.example.fitswap.ui.screens.activeworkout.SessionMenuScreen
import com.example.fitswap.ui.screens.activeworkout.SwapExerciseScreen
import com.example.fitswap.ui.screens.routines.EditRoutineScreen
import com.example.fitswap.ui.screens.routines.RoutineDetailScreen
import com.example.fitswap.ui.screens.routines.RoutineListScreen

private const val ROUTINE_ID_ARG = "routineId"
private const val DAY_ID_ARG = "dayId"
private const val EXERCISE_ID_ARG = "exerciseId"
private const val STUB_TITLE_ARG = "title"
private const val NEW_ROUTINE_ID = "new"

private const val ACTIVE_EXERCISE_ROUTE_PATTERN =
    "activeExercise/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}"

private fun routineDetailRoute(routineId: String) = "routineDetail/$routineId"
private fun editRoutineRoute(routineId: String) = "editRoutine/$routineId"
private fun activeExerciseRoute(routineId: String, dayId: String, exerciseId: String) =
    "activeExercise/$routineId/$dayId/$exerciseId"
private fun swapExerciseRoute(routineId: String, dayId: String, exerciseId: String) =
    "swapExercise/$routineId/$dayId/$exerciseId"
private fun notesRoute(routineId: String, dayId: String, exerciseId: String) =
    "notes/$routineId/$dayId/$exerciseId"
private fun sessionMenuRoute(routineId: String, dayId: String, exerciseId: String) =
    "sessionMenu/$routineId/$dayId/$exerciseId"
private fun stubRoute(title: String) = "stub/${Uri.encode(title)}"

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
            onStartWorkout = { dayId, exerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, exerciseId))
            },
            onEditRoutine = { navController.navigate(editRoutineRoute(routineId)) },
        )
    }
}

fun NavGraphBuilder.editRoutineScreen(navController: NavHostController) {
    composable(
        route = "editRoutine/{$ROUTINE_ID_ARG}",
        arguments = listOf(navArgument(ROUTINE_ID_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val pickedExerciseId by backStackEntry.savedStateHandle
            .getStateFlow<String?>(PICKED_EXERCISE_ID_KEY, null)
            .collectAsStateWithLifecycle()

        EditRoutineScreen(
            onBack = { navController.popBackStack() },
            onRoutineDeleted = {
                // No usar popBackStack(): si se llegó acá vía Detalle de rutina, esa pantalla
                // quedaría observando una rutina que ya no existe. Volver siempre a Rutinas.
                navController.navigate(Destination.Routines.route) {
                    popUpTo(Destination.Routines.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onPickExercise = { openExercisePicker(navController) },
            pickedExerciseId = pickedExerciseId,
            onPickedExerciseConsumed = { backStackEntry.savedStateHandle[PICKED_EXERCISE_ID_KEY] = null },
        )
    }
}

fun NavGraphBuilder.activeExerciseScreen(navController: NavHostController) {
    composable(
        route = ACTIVE_EXERCISE_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.StringType },
            navArgument(DAY_ID_ARG) { type = NavType.StringType },
            navArgument(EXERCISE_ID_ARG) { type = NavType.StringType },
        )
    ) { backStackEntry ->
        val routineId = backStackEntry.arguments?.getString(ROUTINE_ID_ARG).orEmpty()
        val dayId = backStackEntry.arguments?.getString(DAY_ID_ARG).orEmpty()
        val exerciseId = backStackEntry.arguments?.getString(EXERCISE_ID_ARG).orEmpty()
        ActiveExerciseScreen(
            onBack = { navController.popBackStack() },
            onSwapExercise = { navController.navigate(swapExerciseRoute(routineId, dayId, exerciseId)) },
            onOpenNotes = { navController.navigate(notesRoute(routineId, dayId, exerciseId)) },
            onOpenSessionMenu = { navController.navigate(sessionMenuRoute(routineId, dayId, exerciseId)) },
            onAddMedia = { navController.navigate(stubRoute("Galería")) },
            onExerciseAutoAdvance = { nextExerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, nextExerciseId)) {
                    popUpTo(ACTIVE_EXERCISE_ROUTE_PATTERN) { inclusive = true }
                }
            },
        )
    }
}

fun NavGraphBuilder.swapExerciseScreen(navController: NavHostController) {
    composable(
        route = "swapExercise/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}",
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.StringType },
            navArgument(DAY_ID_ARG) { type = NavType.StringType },
            navArgument(EXERCISE_ID_ARG) { type = NavType.StringType },
        )
    ) {
        SwapExerciseScreen(onClose = { navController.popBackStack() })
    }
}

fun NavGraphBuilder.notesScreen(navController: NavHostController) {
    composable(
        route = "notes/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}",
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.StringType },
            navArgument(DAY_ID_ARG) { type = NavType.StringType },
            navArgument(EXERCISE_ID_ARG) { type = NavType.StringType },
        )
    ) {
        NotesScreen(onClose = { navController.popBackStack() })
    }
}

fun NavGraphBuilder.sessionMenuScreen(navController: NavHostController) {
    composable(
        route = "sessionMenu/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}",
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.StringType },
            navArgument(DAY_ID_ARG) { type = NavType.StringType },
            navArgument(EXERCISE_ID_ARG) { type = NavType.StringType },
        )
    ) { backStackEntry ->
        val routineId = backStackEntry.arguments?.getString(ROUTINE_ID_ARG).orEmpty()
        val dayId = backStackEntry.arguments?.getString(DAY_ID_ARG).orEmpty()
        SessionMenuScreen(
            onClose = { navController.popBackStack() },
            onSelectExercise = { newExerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, newExerciseId)) {
                    popUpTo(ACTIVE_EXERCISE_ROUTE_PATTERN) { inclusive = true }
                }
            },
            onFinishRoutine = {
                navController.navigate(Destination.Routines.route) {
                    popUpTo(Destination.Routines.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}

fun NavGraphBuilder.genericStubScreen(navController: NavHostController) {
    composable(
        route = "stub/{$STUB_TITLE_ARG}",
        arguments = listOf(navArgument(STUB_TITLE_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val title = backStackEntry.arguments?.getString(STUB_TITLE_ARG).orEmpty()
        ComingSoonScreen(
            title = title,
            navigationIcon = { BackNavigationIcon(onBack = { navController.popBackStack() }) }
        )
    }
}
