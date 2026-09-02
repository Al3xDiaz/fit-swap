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
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute?.destination?.route,
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
    ) {
        NavHost(navController = navController, startDestination = Destination.Routines.route) {
            routineListScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            routineDetailScreen(navController = navController)
            editRoutineStubScreen(navController = navController)
            activeExerciseScreen(navController = navController)
            swapExerciseScreen(navController = navController)
            notesScreen(navController = navController)
            sessionMenuScreen(navController = navController)
            exerciseCatalogScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            exerciseHistoryScreen(navController = navController)
            summaryScreen(
                navController = navController,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            genericStubScreen(navController = navController)

            topLevelDestinations
                .filter { it != Destination.Routines && it != Destination.Exercises && it != Destination.Summary }
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
