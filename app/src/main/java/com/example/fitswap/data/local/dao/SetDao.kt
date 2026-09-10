package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.LoggedSetEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    /** Series registradas de ese slot **en esa fecha** — no todo el histórico acumulado, para que
     * un ejercicio completado un día no aparezca completo al reabrirlo otro día. */
    @Query("SELECT * FROM logged_sets WHERE routineExerciseId = :routineExerciseId AND date = :date")
    fun observeLoggedSets(routineExerciseId: String, date: LocalDate): Flow<List<LoggedSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loggedSet: LoggedSetEntity)

    /** Borra las series de un slot puntual en una fecha — usado para "descartar" un ejercicio. */
    @Query("DELETE FROM logged_sets WHERE routineExerciseId = :routineExerciseId AND date = :date")
    suspend fun deleteLoggedSetsForDate(routineExerciseId: String, date: LocalDate)

    /** Borra TODAS las series de una fecha, de cualquier ejercicio — usado al descartar toda la
     * sesión de hoy al terminar la rutina. */
    @Query("DELETE FROM logged_sets WHERE date = :date")
    suspend fun deleteAllLoggedSetsForDate(date: LocalDate)
}
