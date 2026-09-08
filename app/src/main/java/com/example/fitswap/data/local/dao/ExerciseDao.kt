package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitswap.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExercise(id: String): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    /** Usado por `DatabaseSeeder` para sincronizar el catálogo en cada arranque sin pisar
     * ejercicios existentes (ver `syncCatalog`). */
    @Query("SELECT id FROM exercises")
    suspend fun allIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)

    /** A diferencia de [insert] (usado para altas y seed), no hace un DELETE+INSERT físico —
     * evita que un `UPDATE` dispare el `ON DELETE CASCADE` de tablas hijas (galería, historial,
     * etc.) que referencian este id aunque no cambie. */
    @Update
    suspend fun update(exercise: ExerciseEntity)

    /** Usado para bloquear el borrado de un ejercicio que sigue en uso en alguna rutina. */
    @Query("SELECT COUNT(*) FROM routine_exercises WHERE exerciseId = :exerciseId")
    suspend fun routineUsageCount(exerciseId: String): Int

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun delete(id: String)
}
