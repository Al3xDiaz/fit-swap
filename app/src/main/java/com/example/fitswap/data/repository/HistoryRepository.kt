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

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todo el historial. */
    suspend fun replaceAllHistory(points: List<HistoryPoint>)

    /** Borra puntos de historial ya persistidos por id — usado para descartar el progreso de la
     * sesión en curso cuando el usuario sale del entrenamiento sin completar el ejercicio. */
    suspend fun deleteHistoryPoints(ids: List<String>)
}
