package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitswap.data.local.entity.HistoryPointEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_points WHERE exerciseId = :exerciseId")
    fun observeHistory(exerciseId: String): Flow<List<HistoryPointEntity>>

    /** Todos los puntos (calentamiento/aproximación/efectivos) de un ejercicio en una fecha —
     * usado por la pantalla de detalle del historial para editarlos/borrarlos individualmente. */
    @Query("SELECT * FROM history_points WHERE exerciseId = :exerciseId AND date = :date")
    fun observeHistoryForDate(exerciseId: String, date: LocalDate): Flow<List<HistoryPointEntity>>

    /** Borra todos los puntos de esa fecha — usado al eliminar un registro del historial. */
    @Query("DELETE FROM history_points WHERE exerciseId = :exerciseId AND date = :date")
    suspend fun deleteHistoryForDate(exerciseId: String, date: LocalDate)

    /** Borra los puntos de TODOS los ejercicios en esa fecha — usado al descartar toda la sesión
     * de hoy al terminar la rutina. */
    @Query("DELETE FROM history_points WHERE date = :date")
    suspend fun deleteAllForDate(date: LocalDate)

    @Query("SELECT * FROM history_points")
    fun observeAllHistory(): Flow<List<HistoryPointEntity>>

    @Query(
        """
        SELECT * FROM history_points
        WHERE exerciseId = :exerciseId AND type = 'EFFECTIVE'
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun lastEffectivePoint(exerciseId: String): HistoryPointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: HistoryPointEntity)

    @Query("DELETE FROM history_points")
    suspend fun clearAll()

    @Query("DELETE FROM history_points WHERE exerciseId = :exerciseId")
    suspend fun deleteAllForExercise(exerciseId: String)

    @Query("DELETE FROM history_points WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<HistoryPointEntity>)

    @Transaction
    suspend fun replaceAll(points: List<HistoryPointEntity>) {
        clearAll()
        insertAll(points)
    }
}
