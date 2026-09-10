package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface CardioSessionRepository {
    fun observeSessions(exerciseId: String): Flow<List<CardioSession>>
    suspend fun addSession(session: CardioSession)

    /** Upsert por id — usado para editar una sesión ya guardada desde el historial. */
    suspend fun updateSession(session: CardioSession)

    suspend fun deleteSession(id: String)

    /** Borra las sesiones de TODOS los ejercicios en una fecha — usado al descartar toda la
     * sesión de hoy al terminar la rutina (ver `RoutineSessionFinisher`). */
    suspend fun deleteAllSessionsForDate(date: LocalDate)
}
