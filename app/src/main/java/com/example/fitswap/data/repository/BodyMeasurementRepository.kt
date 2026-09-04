package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.BodyMeasurementEntry
import kotlinx.coroutines.flow.Flow

interface BodyMeasurementRepository {
    /** Más reciente primero. */
    fun observeMeasurements(): Flow<List<BodyMeasurementEntry>>
    suspend fun addMeasurement(entry: BodyMeasurementEntry)
    suspend fun deleteMeasurement(id: String)
}
