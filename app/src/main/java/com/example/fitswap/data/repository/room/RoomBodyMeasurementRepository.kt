package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.BodyMeasurementDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.domain.model.BodyMeasurementEntry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomBodyMeasurementRepository @Inject constructor(
    private val bodyMeasurementDao: BodyMeasurementDao,
) : BodyMeasurementRepository {

    override fun observeMeasurements(): Flow<List<BodyMeasurementEntry>> =
        bodyMeasurementDao.observeMeasurements()
            .map { entities -> entities.map { it.toDomain() }.sortedByDescending { it.date } }

    override suspend fun addMeasurement(entry: BodyMeasurementEntry) {
        bodyMeasurementDao.insert(entry.toEntity())
    }

    override suspend fun updateMeasurement(entry: BodyMeasurementEntry) {
        bodyMeasurementDao.insert(entry.toEntity()) // upsert por id (REPLACE), igual que addMeasurement
    }

    override suspend fun deleteMeasurement(id: String) {
        bodyMeasurementDao.delete(id)
    }
}
