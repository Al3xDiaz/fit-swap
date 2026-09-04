package com.example.fitswap.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme { LIGHT, DARK, SYSTEM }

@Serializable
enum class UiDensity { COMPACT, COMFORTABLE }

@Serializable
enum class UnitSystem { METRIC, IMPERIAL }

/** Paletas de color seleccionables además del toggle claro/oscuro (ver `ui/theme/Theme.kt`).
 * `DYNAMIC` mantiene el comportamiento previo (Material You basado en el wallpaper, Android 12+). */
@Serializable
enum class ColorPalette { DYNAMIC, CLASSIC, ENERGY, STEEL, NEON }

@Serializable
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val uiDensity: UiDensity = UiDensity.COMFORTABLE,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    /** Descanso entre series, igual para todo ejercicio (ver `SetPlanner.DEFAULT_REST_SECONDS`). */
    val restTimerSeconds: Int = 90,
    val colorPalette: ColorPalette = ColorPalette.DYNAMIC,
)
