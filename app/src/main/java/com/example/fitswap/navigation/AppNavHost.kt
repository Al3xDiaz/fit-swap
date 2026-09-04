package com.example.fitswap.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitswap.ui.common.ComingSoonScreen
import com.example.fitswap.ui.common.MenuNavigationIcon
import com.example.fitswap.ui.screens.activeworkout.SessionDrawerContent
import kotlinx.coroutines.launch

/** Rutas del flujo de rutina activa — en Swap/Notas el drawer sigue siendo el de navegación
 * normal (ver `drawerContent` más abajo), así que ahí el gesto de swipe se bloquea para no poder
 * "fugarse" de la rutina en curso. En ejercicio activo el drawer pasa a ser [SessionDrawerContent],
 * así que el gesto es seguro y se deja habilitado. */
private val ROUTINE_ACTIVE_FLOW_ROUTES = setOf(
    ACTIVE_EXERCISE_ROUTE_PATTERN,
    SWAP_EXERCISE_ROUTE_PATTERN,
    NOTES_ROUTE_PATTERN,
)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()
    val currentDestinationRoute = currentRoute?.destination?.route
    val isActiveExerciseRoute = currentDestinationRoute == ACTIVE_EXERCISE_ROUTE_PATTERN
    val isRoutineActiveFlow = currentDestinationRoute in ROUTINE_ACTIVE_FLOW_ROUTES
    // Solo bloquea el gesto cuando el drawer que se abriría es el de navegación normal (Swap/
    // Notas) — en ejercicio activo abre SessionDrawerContent, donde el gesto es seguro.
    val isLockedNavigationFlow = isRoutineActiveFlow && !isActiveExerciseRoute

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLockedNavigationFlow,
        drawerContent = {
            val activeExerciseEntry = currentRoute
            if (isActiveExerciseRoute && activeExerciseEntry != null) {
                SessionDrawerContent(
                    backStackEntry = activeExerciseEntry,
                    onSelectExercise = { newExerciseId ->
                        scope.launch { drawerState.close() }
                        val routineId = activeExerciseEntry.arguments?.getString("routineId").orEmpty()
                        val dayId = activeExerciseEntry.arguments?.getString("dayId").orEmpty()
                        navController.navigate(activeExerciseRoute(routineId, dayId, newExerciseId)) {
                            popUpTo(ACTIVE_EXERCISE_ROUTE_PATTERN) { inclusive = true }
                        }
                    },
                    onFinishRoutine = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Destination.Routines.route) {
                            popUpTo(Destination.Routines.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onClose = { scope.launch { drawerState.close() } },
                )
            } else {
                DrawerContent(
                    currentRoute = currentDestinationRoute,
                    onDestinationClick = { destination ->
                        scope.launch { drawerState.close() }
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = Destination.Routines.route) {
            routineListScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            routineDetailScreen(navController = navController)
            editRoutineScreen(navController = navController)
            activeExerciseScreen(
                navController = navController,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            swapExerciseScreen(navController = navController)
            notesScreen(navController = navController)
            exerciseCatalogScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            exerciseHistoryScreen(navController = navController)
            exerciseFormScreen(navController = navController)
            exercisePickerScreen(navController = navController)
            summaryScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            toolsScreen(onMenuClick = { scope.launch { drawerState.open() } })
            settingsScreen(onMenuClick = { scope.launch { drawerState.open() } })
            bodyMeasurementsScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            addBodyMeasurementScreen(navController = navController)

            topLevelDestinations
                .filter {
                    it != Destination.Routines && it != Destination.Exercises &&
                        it != Destination.Summary && it != Destination.Tools && it != Destination.Settings &&
                        it != Destination.Measurements
                }
                .forEach { destination ->
                    composable(destination.route) {
                        ComingSoonScreen(
                            title = destination.label,
                            navigationIcon = { MenuNavigationIcon(onMenuClick = { scope.launch { drawerState.open() } }) }
                        )
                    }
                }
        }
    }
}
