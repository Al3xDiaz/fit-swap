package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitswap.ui.screens.measurements.AddBodyMeasurementScreen
import com.example.fitswap.ui.screens.measurements.BodyMeasurementsScreen

private const val ADD_BODY_MEASUREMENT_ROUTE = "addBodyMeasurement"

fun NavGraphBuilder.bodyMeasurementsScreen(navController: NavHostController, onMenuClick: () -> Unit) {
    composable(Destination.Measurements.route) {
        BodyMeasurementsScreen(
            onMenuClick = onMenuClick,
            onAddMeasurement = { navController.navigate(ADD_BODY_MEASUREMENT_ROUTE) },
        )
    }
}

fun NavGraphBuilder.addBodyMeasurementScreen(navController: NavHostController) {
    composable(ADD_BODY_MEASUREMENT_ROUTE) {
        AddBodyMeasurementScreen(onClose = { navController.popBackStack() })
    }
}
