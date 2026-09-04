package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitswap.ui.screens.activeworkout.ActiveExerciseScreen
import com.example.fitswap.ui.screens.activeworkout.NotesScreen
import com.example.fitswap.ui.screens.activeworkout.SwapExerciseScreen
import com.example.fitswap.ui.screens.routines.EditRoutineScreen
import com.example.fitswap.ui.screens.routines.RoutineDetailScreen
import com.example.fitswap.ui.screens.routines.RoutineListScreen

private const val ROUTINE_ID_ARG = "routineId"
private const val DAY_ID_ARG = "dayId"
private const val EXERCISE_ID_ARG = "exerciseId"
private const val NEW_ROUTINE_ID = "new"

/** Visibles desde `AppNavHost.kt` (mismo módulo) — ahí se usan para decidir cuándo el drawer
 * principal debe reemplazarse por [com.example.fitswap.ui.screens.activeworkout.SessionDrawerContent]
 * y cuándo deshabilitar su gesto de swipe (mientras hay una rutina activa). */
internal const val ACTIVE_EXERCISE_ROUTE_PATTERN =
    "activeExercise/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}"
internal const val SWAP_EXERCISE_ROUTE_PATTERN =
    "swapExercise/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}"
internal const val NOTES_ROUTE_PATTERN =
    "notes/{$ROUTINE_ID_ARG}/{$DAY_ID_ARG}/{$EXERCISE_ID_ARG}"

private fun routineDetailRoute(routineId: String) = "routineDetail/$routineId"
private fun editRoutineRoute(routineId: String) = "editRoutine/$routineId"
internal fun activeExerciseRoute(routineId: String, dayId: String, exerciseId: String) =
    "activeExercise/$routineId/$dayId/$exerciseId"
private fun swapExerciseRoute(routineId: String, dayId: String, exerciseId: String) =
    "swapExercise/$routineId/$dayId/$exerciseId"
private fun notesRoute(routineId: String, dayId: String, exerciseId: String) =
    "notes/$routineId/$dayId/$exerciseId"

fun NavGraphBuilder.routineListScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Routines.route) {
        RoutineListScreen(
            onMenuClick = onMenuClick,
            onRoutineClick = { routineId -> navController.navigate(routineDetailRoute(routineId)) },
            onNewRoutineClick = { navController.navigate(editRoutineRoute(NEW_ROUTINE_ID)) },
            onStartWorkout = { routineId, dayId, exerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, exerciseId))
            },
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

fun NavGraphBuilder.activeExerciseScreen(navController: NavHostController, onOpenDrawer: () -> Unit) {
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
            onExitDiscarding = {
                navController.navigate(Destination.Routines.route) {
                    popUpTo(Destination.Routines.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onSwapExercise = { navController.navigate(swapExerciseRoute(routineId, dayId, exerciseId)) },
            onOpenNotes = { navController.navigate(notesRoute(routineId, dayId, exerciseId)) },
            onOpenDrawer = onOpenDrawer,
            onPreviousExercise = { previousExerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, previousExerciseId)) {
                    popUpTo(ACTIVE_EXERCISE_ROUTE_PATTERN) { inclusive = true }
                }
            },
            onNextExercise = { nextExerciseId ->
                navController.navigate(activeExerciseRoute(routineId, dayId, nextExerciseId)) {
                    popUpTo(ACTIVE_EXERCISE_ROUTE_PATTERN) { inclusive = true }
                }
            },
        )
    }
}

fun NavGraphBuilder.swapExerciseScreen(navController: NavHostController) {
    composable(
        route = SWAP_EXERCISE_ROUTE_PATTERN,
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
        route = NOTES_ROUTE_PATTERN,
        arguments = listOf(
            navArgument(ROUTINE_ID_ARG) { type = NavType.StringType },
            navArgument(DAY_ID_ARG) { type = NavType.StringType },
            navArgument(EXERCISE_ID_ARG) { type = NavType.StringType },
        )
    ) {
        NotesScreen(onClose = { navController.popBackStack() })
    }
}
