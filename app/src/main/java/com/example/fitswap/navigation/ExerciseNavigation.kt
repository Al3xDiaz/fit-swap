package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitswap.ui.common.BackNavigationIcon
import com.example.fitswap.ui.common.MenuNavigationIcon
import com.example.fitswap.ui.screens.exercises.CardioSessionEditScreen
import com.example.fitswap.ui.screens.exercises.ExerciseCatalogScreen
import com.example.fitswap.ui.screens.exercises.ExerciseFormScreen
import com.example.fitswap.ui.screens.exercises.ExerciseHistoryScreen
import com.example.fitswap.ui.screens.exercises.NEW_EXERCISE_ID

private const val CATALOG_EXERCISE_ID_ARG = "exerciseId"
private const val CARDIO_SESSION_ID_ARG = "sessionId"
private const val EXERCISE_PICKER_ROUTE = "exercisePicker"

/** Clave del resultado que `exercisePickerScreen` deja en el backstack entry anterior (Editar rutina). */
const val PICKED_EXERCISE_ID_KEY = "pickedExerciseId"

private fun exerciseHistoryRoute(exerciseId: String) = "exerciseHistory/$exerciseId"
private fun exerciseFormRoute(exerciseId: String) = "exerciseForm/$exerciseId"
private fun cardioSessionEditRoute(exerciseId: String, sessionId: String) = "cardioSessionEdit/$exerciseId/$sessionId"

fun NavGraphBuilder.exerciseCatalogScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Exercises.route) {
        ExerciseCatalogScreen(
            navigationIcon = { MenuNavigationIcon(onMenuClick = onMenuClick) },
            onExerciseClick = { exerciseId -> navController.navigate(exerciseHistoryRoute(exerciseId)) },
            onAddExercise = { navController.navigate(exerciseFormRoute(NEW_EXERCISE_ID)) },
        )
    }
}

fun NavGraphBuilder.exerciseHistoryScreen(navController: NavHostController) {
    composable(
        route = "exerciseHistory/{$CATALOG_EXERCISE_ID_ARG}",
        arguments = listOf(navArgument(CATALOG_EXERCISE_ID_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val exerciseId = backStackEntry.arguments?.getString(CATALOG_EXERCISE_ID_ARG).orEmpty()
        ExerciseHistoryScreen(
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(exerciseFormRoute(exerciseId)) },
            onEditCardioSession = { sessionId ->
                navController.navigate(cardioSessionEditRoute(exerciseId, sessionId))
            },
        )
    }
}

fun NavGraphBuilder.cardioSessionEditScreen(navController: NavHostController) {
    composable(
        route = "cardioSessionEdit/{$CATALOG_EXERCISE_ID_ARG}/{$CARDIO_SESSION_ID_ARG}",
        arguments = listOf(
            navArgument(CATALOG_EXERCISE_ID_ARG) { type = NavType.StringType },
            navArgument(CARDIO_SESSION_ID_ARG) { type = NavType.StringType },
        )
    ) {
        CardioSessionEditScreen(onClose = { navController.popBackStack() })
    }
}

fun NavGraphBuilder.exerciseFormScreen(navController: NavHostController) {
    composable(
        route = "exerciseForm/{$CATALOG_EXERCISE_ID_ARG}",
        arguments = listOf(navArgument(CATALOG_EXERCISE_ID_ARG) { type = NavType.StringType })
    ) {
        ExerciseFormScreen(onClose = { navController.popBackStack() })
    }
}

/** Reusa el catálogo de M6 en modo "elegir" para "+ Agregar ejercicio" de Editar rutina (M8):
 * en vez de abrir el historial, devuelve el id elegido a la pantalla anterior y cierra. */
fun NavGraphBuilder.exercisePickerScreen(navController: NavHostController) {
    composable(EXERCISE_PICKER_ROUTE) {
        ExerciseCatalogScreen(
            navigationIcon = { BackNavigationIcon(onBack = { navController.popBackStack() }) },
            onExerciseClick = { exerciseId ->
                navController.previousBackStackEntry?.savedStateHandle?.set(PICKED_EXERCISE_ID_KEY, exerciseId)
                navController.popBackStack()
            },
        )
    }
}

fun openExercisePicker(navController: NavHostController) {
    navController.navigate(EXERCISE_PICKER_ROUTE)
}
