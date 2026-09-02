package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val EXERCISE_ID = "press-de-pecho"

private fun point(date: LocalDate, type: SetType, reps: Int, weightKg: Double, seq: Int = 0) = HistoryPoint(
    id = "$date-$type-$seq",
    exerciseId = EXERCISE_ID,
    date = date,
    type = type,
    reps = reps,
    weightKg = weightKg,
)

class ExerciseHistoryAnalyticsTest {

    @Test
    fun `el volumen y el peso maximo excluyen series de calentamiento y aproximacion`() {
        val points = listOf(
            point(LocalDate.of(2026, 8, 4), SetType.WARMUP, reps = 10, weightKg = 20.0),
            point(LocalDate.of(2026, 8, 4), SetType.APPROACH, reps = 8, weightKg = 40.0),
            point(LocalDate.of(2026, 8, 4), SetType.EFFECTIVE, reps = 10, weightKg = 60.0, seq = 1),
            point(LocalDate.of(2026, 8, 4), SetType.EFFECTIVE, reps = 10, weightKg = 60.0, seq = 2),
        )

        val volume = ExerciseHistoryAnalytics.totalEffectiveVolumeKg(points)
        val maxWeight = ExerciseHistoryAnalytics.maxEffectiveWeightKg(points)

        assertEquals(1200.0, volume, 0.0) // 2 x (10 reps x 60kg), sin contar warmup/approach
        assertEquals(60.0, maxWeight)
    }

    @Test
    fun `sin series efectivas, el peso maximo es nulo y el volumen es cero`() {
        val points = listOf(
            point(LocalDate.of(2026, 8, 4), SetType.WARMUP, reps = 10, weightKg = 20.0),
        )

        assertNull(ExerciseHistoryAnalytics.maxEffectiveWeightKg(points))
        assertEquals(0.0, ExerciseHistoryAnalytics.totalEffectiveVolumeKg(points), 0.0)
    }

    @Test
    fun `las sesiones recientes se agrupan por fecha y se ordenan de mas reciente a mas antigua`() {
        val points = listOf(
            point(LocalDate.of(2026, 8, 4), SetType.EFFECTIVE, reps = 10, weightKg = 55.0, seq = 1),
            point(LocalDate.of(2026, 8, 4), SetType.EFFECTIVE, reps = 10, weightKg = 55.0, seq = 2),
            point(LocalDate.of(2026, 8, 18), SetType.EFFECTIVE, reps = 8, weightKg = 60.0, seq = 1),
        )

        val sessions = ExerciseHistoryAnalytics.recentSessions(points)

        assertEquals(2, sessions.size)
        assertEquals(LocalDate.of(2026, 8, 18), sessions.first().date)
        assertEquals(1, sessions.first().setCount)
        assertEquals(480.0, sessions.first().volumeKg, 0.0) // 8 reps x 60kg
        assertEquals(LocalDate.of(2026, 8, 4), sessions[1].date)
        assertEquals(2, sessions[1].setCount)
    }

    @Test
    fun `recentSessions respeta el limite pedido`() {
        val points = (1..10).map { day ->
            point(LocalDate.of(2026, 8, day), SetType.EFFECTIVE, reps = 10, weightKg = 50.0)
        }

        val sessions = ExerciseHistoryAnalytics.recentSessions(points, limit = 3)

        assertEquals(3, sessions.size)
    }
}
