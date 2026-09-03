package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.HistoryDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.domain.model.HistoryPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomHistoryRepository @Inject constructor(private val historyDao: HistoryDao) : HistoryRepository {

    override suspend fun lastWeightKg(exerciseId: String): Double? =
        historyDao.lastEffectivePoint(exerciseId)?.weightKg

    override fun observeHistory(exerciseId: String): Flow<List<HistoryPoint>> =
        historyDao.observeHistory(exerciseId).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllHistory(): Flow<List<HistoryPoint>> =
        historyDao.observeAllHistory().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addHistoryPoint(point: HistoryPoint) {
        historyDao.insert(point.toEntity())
    }

    override suspend fun replaceAllHistory(points: List<HistoryPoint>) {
        historyDao.replaceAll(points.map { it.toEntity() })
    }
}
