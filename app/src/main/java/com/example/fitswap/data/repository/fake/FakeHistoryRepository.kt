package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeHistoryRepository @Inject constructor() : HistoryRepository {

    private val historyByExercise = MutableStateFlow(buildHistorySeed())

    override suspend fun lastWeightKg(exerciseId: String): Double? =
        historyByExercise.value[exerciseId]
            ?.filter { it.type == SetType.EFFECTIVE }
            ?.maxByOrNull { it.date }
            ?.weightKg

    override fun observeHistory(exerciseId: String): Flow<List<HistoryPoint>> =
        historyByExercise.map { it[exerciseId].orEmpty() }

    override fun observeHistoryForDate(exerciseId: String, date: LocalDate): Flow<List<HistoryPoint>> =
        historyByExercise.map { it[exerciseId].orEmpty().filter { point -> point.date == date } }

    override fun observeAllHistory(): Flow<List<HistoryPoint>> =
        historyByExercise.map { it.values.flatten() }

    override suspend fun addHistoryPoint(point: HistoryPoint) {
        upsert(point)
    }

    override suspend fun updateHistoryPoint(point: HistoryPoint) {
        upsert(point)
    }

    override suspend fun replaceAllHistory(points: List<HistoryPoint>) {
        historyByExercise.value = points.groupBy { it.exerciseId }
    }

    override suspend fun deleteHistoryPoints(ids: List<String>) {
        historyByExercise.update { current ->
            current.mapValues { (_, points) -> points.filterNot { it.id in ids } }
        }
    }

    override suspend fun deleteHistoryForDate(exerciseId: String, date: LocalDate) {
        historyByExercise.update { current ->
            val existing = current[exerciseId].orEmpty().filterNot { it.date == date }
            current + (exerciseId to existing)
        }
    }

    override suspend fun deleteAllHistoryForDate(date: LocalDate) {
        historyByExercise.update { current ->
            current.mapValues { (_, points) -> points.filterNot { it.date == date } }
        }
    }

    /** Upsert por id, igual que hace Room con `OnConflictStrategy.REPLACE`. */
    private fun upsert(point: HistoryPoint) {
        historyByExercise.update { current ->
            val existing = current[point.exerciseId].orEmpty().filterNot { it.id == point.id }
            current + (point.exerciseId to (existing + point))
        }
    }
}

private fun points(
    exerciseId: String,
    date: LocalDate,
    type: SetType,
    weightKg: Double,
    reps: Int,
    count: Int,
): List<HistoryPoint> = (1..count).map { setNumber ->
    HistoryPoint(
        id = "$exerciseId-$date-$type-$setNumber",
        exerciseId = exerciseId,
        date = date,
        type = type,
        reps = reps,
        weightKg = weightKg,
    )
}

/**
 * Solo "press-de-pecho" y "sentadilla-hack-prensa" tienen historial previo (mismo criterio que M3:
 * ejercitar ambas ramas de "peso sugerido = último registrado, si no 1 kg"), ahora con varias
 * sesiones para que Historial de ejercicio tenga con qué mostrarse. La sesión más reciente de cada
 * uno incluye series de aproximación para poder verificar que el volumen/peso máximo las excluyen.
 */
internal fun buildHistorySeed(): Map<String, List<HistoryPoint>> {
    val pressDePecho = "press-de-pecho"
    val sentadilla = "sentadilla-hack-prensa"

    val pressHistory =
        points(pressDePecho, LocalDate.of(2026, 8, 4), SetType.EFFECTIVE, weightKg = 55.0, reps = 10, count = 4) +
            points(pressDePecho, LocalDate.of(2026, 8, 11), SetType.EFFECTIVE, weightKg = 57.5, reps = 10, count = 4) +
            points(pressDePecho, LocalDate.of(2026, 8, 18), SetType.APPROACH, weightKg = 36.0, reps = 8, count = 1) +
            points(pressDePecho, LocalDate.of(2026, 8, 18), SetType.EFFECTIVE, weightKg = 60.0, reps = 8, count = 4)

    val sentadillaHistory =
        points(sentadilla, LocalDate.of(2026, 8, 5), SetType.EFFECTIVE, weightKg = 75.0, reps = 8, count = 4) +
            points(sentadilla, LocalDate.of(2026, 8, 12), SetType.EFFECTIVE, weightKg = 77.5, reps = 8, count = 4) +
            points(sentadilla, LocalDate.of(2026, 8, 19), SetType.APPROACH, weightKg = 48.0, reps = 6, count = 2) +
            points(sentadilla, LocalDate.of(2026, 8, 19), SetType.EFFECTIVE, weightKg = 80.0, reps = 6, count = 4)

    return mapOf(
        pressDePecho to pressHistory,
        sentadilla to sentadillaHistory,
    )
}
