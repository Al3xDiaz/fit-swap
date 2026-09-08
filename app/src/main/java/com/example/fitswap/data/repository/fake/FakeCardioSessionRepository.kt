package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.domain.model.CardioSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeCardioSessionRepository @Inject constructor() : CardioSessionRepository {

    private val sessionsByExercise = MutableStateFlow<Map<String, List<CardioSession>>>(emptyMap())

    override fun observeSessions(exerciseId: String): Flow<List<CardioSession>> =
        sessionsByExercise.map { it[exerciseId].orEmpty() }

    override suspend fun addSession(session: CardioSession) {
        upsert(session)
    }

    override suspend fun updateSession(session: CardioSession) {
        upsert(session)
    }

    override suspend fun deleteSession(id: String) {
        sessionsByExercise.update { current ->
            current.mapValues { (_, sessions) -> sessions.filterNot { it.id == id } }
        }
    }

    /** Upsert por id, igual que hace Room con `OnConflictStrategy.REPLACE` — reemplaza la sesión
     * existente con el mismo id en vez de appendear siempre una nueva. */
    private fun upsert(session: CardioSession) {
        sessionsByExercise.update { current ->
            val existing = current[session.exerciseId].orEmpty().filterNot { it.id == session.id }
            current + (session.exerciseId to (existing + session))
        }
    }
}
