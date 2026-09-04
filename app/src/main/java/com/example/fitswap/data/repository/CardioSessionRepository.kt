package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.CardioSession
import kotlinx.coroutines.flow.Flow

interface CardioSessionRepository {
    fun observeSessions(exerciseId: String): Flow<List<CardioSession>>
    suspend fun addSession(session: CardioSession)
}
