package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.CardioSessionDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.domain.model.CardioSession
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
}
