package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

private fun point(exerciseId: String, date: LocalDate, type: SetType = SetType.EFFECTIVE, reps: Int = 10, weightKg: Double = 10.0, seq: Int = 0) =
    HistoryPoint(id = "$exerciseId-$date-$type-$seq", exerciseId = exerciseId, date = date, type = type, reps = reps, weightKg = weightKg)

class SummaryLogicTest {

    @Test
    fun `cuenta un entrenamiento completado por cada dia distinto con series efectivas este mes`() {
        val today = LocalDate.of(2026, 9, 1)
        val history = listOf(
            point("a", LocalDate.of(2026, 9, 1)),
            point("b", LocalDate.of(2026, 9, 1), seq = 1), // mismo día, otro ejercicio -> no duplica
            point("a", LocalDate.of(2026, 8, 30)), // mes anterior -> no cuenta
        )

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(1, report.completedWorkoutsThisMonth)
    }

    @Test
    fun `un dia con solo calentamiento o aproximacion no cuenta como entrenamiento completado`() {
        val today = LocalDate.of(2026, 9, 1)
        val history = listOf(point("a", LocalDate.of(2026, 9, 1), type = SetType.WARMUP))

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(0, report.completedWorkoutsThisMonth)
    }

    @Test
    fun `el volumen mensual excluye calentamiento y aproximacion`() {
        val today = LocalDate.of(2026, 9, 1)
        val history = listOf(
            point("a", LocalDate.of(2026, 9, 1), type = SetType.WARMUP, reps = 10, weightKg = 20.0),
            point("a", LocalDate.of(2026, 9, 1), type = SetType.EFFECTIVE, reps = 10, weightKg = 50.0, seq = 1),
        )

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(YearMonth.of(2026, 9), report.monthlyVolumes.first().yearMonth)
        assertEquals(500.0, report.monthlyVolumes.first().volumeKg, 0.0)
    }

    @Test
    fun `monthlyVolumes devuelve el mes actual primero y respeta monthsToShow`() {
        val today = LocalDate.of(2026, 9, 15)
        val history = listOf(
            point("a", LocalDate.of(2026, 9, 1), weightKg = 10.0, reps = 1),
            point("a", LocalDate.of(2026, 8, 1), weightKg = 20.0, reps = 1),
            point("a", LocalDate.of(2026, 7, 1), weightKg = 30.0, reps = 1),
        )

        val report = SummaryLogic.buildReport(history, today, monthsToShow = 3)

        assertEquals(3, report.monthlyVolumes.size)
        assertEquals(YearMonth.of(2026, 9), report.monthlyVolumes[0].yearMonth)
        assertEquals(10.0, report.monthlyVolumes[0].volumeKg, 0.0)
        assertEquals(YearMonth.of(2026, 8), report.monthlyVolumes[1].yearMonth)
        assertEquals(YearMonth.of(2026, 7), report.monthlyVolumes[2].yearMonth)
    }

    @Test
    fun `sin historial la racha es cero`() {
        val report = SummaryLogic.buildReport(emptyList(), LocalDate.of(2026, 9, 1))

        assertEquals(0, report.currentStreakWeeks)
    }

    @Test
    fun `la racha cuenta semanas ISO consecutivas con entrenamiento hasta el primer hueco`() {
        // Martes 2026-09-01 es la semana del 31 ago al 6 sep.
        val today = LocalDate.of(2026, 9, 1)
        val history = listOf(
            point("a", LocalDate.of(2026, 9, 1)), // esta semana
            point("a", LocalDate.of(2026, 8, 26)), // semana anterior
            point("a", LocalDate.of(2026, 8, 19)), // dos semanas antes
            point("a", LocalDate.of(2026, 8, 1)), // hay semanas vacías entre esta y la racha -> no se suma
        )

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(3, report.currentStreakWeeks)
    }

    @Test
    fun `la racha se corta si la semana entrenada mas reciente no es esta ni la anterior`() {
        val today = LocalDate.of(2026, 9, 15)
        val history = listOf(point("a", LocalDate.of(2026, 8, 1)))

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(0, report.currentStreakWeeks)
    }

    @Test
    fun `la racha sigue vigente si todavia no se entreno esta semana pero si la anterior`() {
        val today = LocalDate.of(2026, 9, 1) // martes, semana en curso sin entrenar aún
        val history = listOf(
            point("a", LocalDate.of(2026, 8, 26)), // semana anterior
            point("a", LocalDate.of(2026, 8, 19)), // dos semanas antes
        )

        val report = SummaryLogic.buildReport(history, today)

        assertEquals(2, report.currentStreakWeeks)
    }
}
