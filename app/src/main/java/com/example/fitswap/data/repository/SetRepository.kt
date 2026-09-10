package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.LoggedSet
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface SetRepository {
    /** Series registradas de ese slot en esa fecha — no todo el histórico acumulado (ver
     * [LoggedSet.date]): un ejercicio completado un día no debe aparecer completo otro día. */
    fun observeLoggedSets(routineExerciseId: String, date: LocalDate): Flow<List<LoggedSet>>
    suspend fun logSet(loggedSet: LoggedSet)

    /** Borra las series de un ejercicio puntual en una fecha — usado para "descartar" ese
     * ejercicio (ver `ActiveExerciseViewModel.discardExerciseData`). */
    suspend fun deleteLoggedSetsForDate(routineExerciseId: String, date: LocalDate)

    /** Borra TODAS las series de una fecha, de cualquier ejercicio — usado al descartar toda la
     * sesión del día al terminar la rutina (ver `RoutineSessionFinisher`). */
    suspend fun deleteAllLoggedSetsForDate(date: LocalDate)
}
