package com.example.fitswap.data.session

import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.time.CurrentDateProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centraliza qué pasa con lo registrado hoy al terminar una rutina — usado tanto desde el drawer
 * de sesión (`SessionMenuViewModel`) como desde la pantalla de ejercicio activo
 * (`ActiveExerciseViewModel`) para no duplicar esta lógica entre los dos puntos de entrada del
 * modal Guardar/Descartar/Cancelar (`FinishRoutineDialog`).
 */
@Singleton
class RoutineSessionFinisher @Inject constructor(
    private val setRepository: SetRepository,
    private val historyRepository: HistoryRepository,
    private val cardioSessionRepository: CardioSessionRepository,
    private val notesRepository: NotesRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val currentDateProvider: CurrentDateProvider,
) {
    /** Conserva todo lo registrado hoy — solo limpia sustituciones de la sesión. */
    suspend fun saveAndFinish() {
        workoutSessionRepository.clearAll()
    }

    /** Borra TODO lo registrado hoy (series, historial, sesiones de cardio, notas) de cualquier
     * ejercicio de la rutina, y limpia sustituciones. */
    suspend fun discardAndFinish() {
        val today = currentDateProvider.today()
        setRepository.deleteAllLoggedSetsForDate(today)
        historyRepository.deleteAllHistoryForDate(today)
        cardioSessionRepository.deleteAllSessionsForDate(today)
        notesRepository.deleteAllNotesForDate(today)
        workoutSessionRepository.clearAll()
    }
}
