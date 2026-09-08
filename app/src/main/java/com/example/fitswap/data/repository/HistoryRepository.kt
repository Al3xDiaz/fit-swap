package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.HistoryPoint
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    /** Último peso efectivo registrado para el ejercicio, o nulo si no hay historial. */
    suspend fun lastWeightKg(exerciseId: String): Double?

    fun observeHistory(exerciseId: String): Flow<List<HistoryPoint>>

    /** Todos los puntos (calentamiento/aproximación/efectivos) de un ejercicio en una fecha —
     * usado por la pantalla de detalle del historial. */
    fun observeHistoryForDate(exerciseId: String, date: LocalDate): Flow<List<HistoryPoint>>

    /** Historial de todos los ejercicios combinado — usado por Resumen/Reporte para agregados cruzados. */
    fun observeAllHistory(): Flow<List<HistoryPoint>>

    suspend fun addHistoryPoint(point: HistoryPoint)

    /** Upsert por id — usado para editar un punto individual desde el detalle del historial. */
    suspend fun updateHistoryPoint(point: HistoryPoint)

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todo el historial. */
    suspend fun replaceAllHistory(points: List<HistoryPoint>)

    /** Borra puntos de historial ya persistidos por id — usado para descartar el progreso de la
     * sesión en curso cuando el usuario sale del entrenamiento sin completar el ejercicio. */
    suspend fun deleteHistoryPoints(ids: List<String>)

    /** Borra todos los puntos (efectivos y de aproximación) de un ejercicio en una fecha — usado
     * al eliminar un registro completo del historial. */
    suspend fun deleteHistoryForDate(exerciseId: String, date: LocalDate)
}
