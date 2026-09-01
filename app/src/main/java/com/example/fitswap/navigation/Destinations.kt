package com.example.fitswap.navigation

sealed class Destination(val route: String, val label: String) {
    data object Routines : Destination("routines", "Rutinas")
    data object Exercises : Destination("exercises", "Ejercicios")
    data object Summary : Destination("summary", "Resumen/Reporte")
    data object Tools : Destination("tools", "Herramientas")
    data object Settings : Destination("settings", "Configuraciones")
}

val topLevelDestinations: List<Destination> = listOf(
    Destination.Routines,
    Destination.Exercises,
    Destination.Summary,
    Destination.Tools,
    Destination.Settings,
)
