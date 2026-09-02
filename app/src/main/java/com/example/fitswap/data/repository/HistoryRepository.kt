package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.HistoryPoint
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    /** Último peso efectivo registrado para el ejercicio, o nulo si no hay historial. */
    suspend fun lastWeightKg(exerciseId: String): Double?

    fun observeHistory(exerciseId: String): Flow<List<HistoryPoint>>

    /** Historial de todos los ejercicios combinado — usado por Resumen/Reporte para agregados cruzados. */
    fun observeAllHistory(): Flow<List<HistoryPoint>>

    suspend fun addHistoryPoint(point: HistoryPoint)
}
