package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

data class MonthlyVolume(val yearMonth: YearMonth, val volumeKg: Double)

data class SummaryReport(
    val completedWorkoutsThisMonth: Int,
    val currentStreakWeeks: Int,
    /** Más reciente primero. */
    val monthlyVolumes: List<MonthlyVolume>,
)

/**
 * Agregados cruzados entre ejercicios/rutinas para Resumen/Reporte — a diferencia de Historial de
 * ejercicio (M6, por ejercicio individual). No hay un modelo de datos nuevo de "entrenamiento
 * completado": un "día entrenado" se deriva de que exista al menos una serie efectiva registrada
 * ese día, en cualquier ejercicio (ver `docs/DESIGN_DOC.md#resumenreporte`).
 */
object SummaryLogic {

    private val weekFields = WeekFields.ISO

    fun buildReport(allHistory: List<HistoryPoint>, today: LocalDate, monthsToShow: Int = 2): SummaryReport {
        val trainingDates = allHistory
            .filter { it.type == SetType.EFFECTIVE }
            .map { it.date }
            .toSortedSet()

        val currentMonth = YearMonth.from(today)
        val completedThisMonth = trainingDates.count { YearMonth.from(it) == currentMonth }

        val monthlyVolumes = (0 until monthsToShow).map { offset ->
            val month = currentMonth.minusMonths(offset.toLong())
            val volume = allHistory
                .filter { it.type == SetType.EFFECTIVE && YearMonth.from(it.date) == month }
                .sumOf { it.weightKg * it.reps }
            MonthlyVolume(month, volume)
        }

        return SummaryReport(
            completedWorkoutsThisMonth = completedThisMonth,
            currentStreakWeeks = currentStreakWeeks(trainingDates, today),
            monthlyVolumes = monthlyVolumes,
        )
    }

    /**
     * Semanas consecutivas (ISO, lunes a domingo) con al menos un día entrenado, contando hacia
     * atrás desde la más reciente. La racha se considera cortada (0) si la semana entrenada más
     * reciente no es esta semana ni la anterior — es decir, "actual" requiere actividad reciente.
     */
    private fun currentStreakWeeks(trainingDates: Set<LocalDate>, today: LocalDate): Int {
        if (trainingDates.isEmpty()) return 0

        val currentWeekStart = today.with(weekFields.dayOfWeek(), 1L)
        val trainingWeekStarts = trainingDates.map { it.with(weekFields.dayOfWeek(), 1L) }.toSet()

        val mostRecentTrainingWeek = trainingWeekStarts.max()
        if (mostRecentTrainingWeek < currentWeekStart.minusWeeks(1)) return 0

        var streak = 0
        var weekStart = mostRecentTrainingWeek
        while (weekStart in trainingWeekStarts) {
            streak++
            weekStart = weekStart.minusWeeks(1)
        }
        return streak
    }
}
