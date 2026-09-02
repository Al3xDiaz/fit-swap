package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate

data class HistorySession(
    val date: LocalDate,
    val volumeKg: Double,
    val setCount: Int,
)

/**
 * Agregaciones de historial de ejercicio. El volumen y el peso máximo consideran solo series
 * efectivas — calentamiento/aproximación no cuentan como progreso real (ver
 * `docs/DESIGN_DOC.md#modelo-de-datos-borrador`: "el volumen/progreso reportado debe filtrar solo
 * series efectivas").
 */
object ExerciseHistoryAnalytics {

    fun maxEffectiveWeightKg(points: List<HistoryPoint>): Double? =
        points.filter { it.type == SetType.EFFECTIVE }.maxOfOrNull { it.weightKg }

    fun totalEffectiveVolumeKg(points: List<HistoryPoint>): Double =
        points.filter { it.type == SetType.EFFECTIVE }.sumOf { it.weightKg * it.reps }

    fun recentSessions(points: List<HistoryPoint>, limit: Int = 5): List<HistorySession> =
        points.filter { it.type == SetType.EFFECTIVE }
            .groupBy { it.date }
            .map { (date, sessionPoints) ->
                HistorySession(
                    date = date,
                    volumeKg = sessionPoints.sumOf { it.weightKg * it.reps },
                    setCount = sessionPoints.size,
                )
            }
            .sortedByDescending { it.date }
            .take(limit)
}
