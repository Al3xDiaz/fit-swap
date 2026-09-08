package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.CardioSession
import kotlinx.coroutines.flow.Flow

interface CardioSessionRepository {
    fun observeSessions(exerciseId: String): Flow<List<CardioSession>>
    suspend fun addSession(session: CardioSession)

    /** Upsert por id — usado para editar una sesión ya guardada desde el historial. */
    suspend fun updateSession(session: CardioSession)

    suspend fun deleteSession(id: String)
}
