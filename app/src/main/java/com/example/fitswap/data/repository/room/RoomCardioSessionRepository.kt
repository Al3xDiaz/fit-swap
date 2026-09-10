package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.CardioSessionDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomCardioSessionRepository @Inject constructor(
    private val cardioSessionDao: CardioSessionDao,
) : CardioSessionRepository {

    override fun observeSessions(exerciseId: String): Flow<List<CardioSession>> =
        cardioSessionDao.observeSessions(exerciseId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addSession(session: CardioSession) {
        cardioSessionDao.insert(session.toEntity())
    }

    override suspend fun updateSession(session: CardioSession) {
        cardioSessionDao.insert(session.toEntity()) // upsert por id (REPLACE), igual que addSession
    }

    override suspend fun deleteSession(id: String) {
        cardioSessionDao.deleteSession(id)
    }

    override suspend fun deleteAllSessionsForDate(date: LocalDate) {
        cardioSessionDao.deleteAllForDate(date)
    }
}
