package com.example.fitswap.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitswap.ui.screens.measurements.AddBodyMeasurementScreen
import com.example.fitswap.ui.screens.measurements.BodyMeasurementsScreen
import com.example.fitswap.ui.screens.measurements.MeasurementGuideScreen

private const val ADD_BODY_MEASUREMENT_ROUTE = "addBodyMeasurement"
private const val MEASUREMENT_GUIDE_ROUTE = "measurementGuide"

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
        AddBodyMeasurementScreen(
            onClose = { navController.popBackStack() },
            onOpenGuide = { navController.navigate(MEASUREMENT_GUIDE_ROUTE) },
        )
    }
}

fun NavGraphBuilder.measurementGuideScreen(navController: NavHostController) {
    composable(MEASUREMENT_GUIDE_ROUTE) {
        MeasurementGuideScreen(onBack = { navController.popBackStack() })
    }
}
