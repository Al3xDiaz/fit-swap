package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.BodyMeasurementEntry
import kotlinx.coroutines.flow.Flow

interface BodyMeasurementRepository {
    /** Más reciente primero. */
    fun observeMeasurements(): Flow<List<BodyMeasurementEntry>>
    suspend fun addMeasurement(entry: BodyMeasurementEntry)

    /** Upsert por id — usado para editar una medición ya guardada. */
    suspend fun updateMeasurement(entry: BodyMeasurementEntry)

    suspend fun deleteMeasurement(id: String)
}
