package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.CardioSessionEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface CardioSessionDao {
    @Query("SELECT * FROM cardio_sessions WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun observeSessions(exerciseId: String): Flow<List<CardioSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: CardioSessionEntity)

    @Query("DELETE FROM cardio_sessions WHERE exerciseId = :exerciseId")
    suspend fun deleteAllForExercise(exerciseId: String)

    @Query("DELETE FROM cardio_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    /** Borra las sesiones de TODOS los ejercicios en esa fecha — usado al descartar toda la
     * sesión de hoy al terminar la rutina. */
    @Query("DELETE FROM cardio_sessions WHERE date = :date")
    suspend fun deleteAllForDate(date: LocalDate)
}
