package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines")
    fun observeRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_days")
    fun observeDays(): Flow<List<RoutineDayEntity>>

    @Query("SELECT * FROM routine_exercises")
    fun observeRoutineExercises(): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun count(): Int

    @Query("SELECT id FROM routines")
    suspend fun allRoutineIds(): List<String>

    @Query("SELECT id FROM routine_days WHERE routineId = :routineId")
    suspend fun dayIdsForRoutine(routineId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: RoutineDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Query("DELETE FROM routines WHERE id = :routineId AND isDefault = 0")
    suspend fun deleteRoutine(routineId: String)

    @Query("UPDATE routine_days SET name = :newName WHERE id = :dayId")
    suspend fun renameDay(dayId: String, newName: String)

    @Query("DELETE FROM routine_days WHERE id = :dayId")
    suspend fun removeDay(dayId: String)

    @Query("DELETE FROM routine_exercises WHERE id = :routineExerciseId")
    suspend fun removeRoutineExercise(routineExerciseId: String)

    @Query("SELECT * FROM routine_exercises WHERE dayId = :dayId ORDER BY orderIndex")
    suspend fun getExercisesForDay(dayId: String): List<RoutineExerciseEntity>

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM routine_days WHERE routineId = :routineId")
    suspend fun maxDayOrderIndex(routineId: String): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM routine_exercises WHERE dayId = :dayId")
    suspend fun maxExerciseOrderIndex(dayId: String): Int

    @Update
    suspend fun updateRoutineExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routines")
    suspend fun clearRoutines()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<RoutineDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    @Transaction
    suspend fun replaceAll(
        routines: List<RoutineEntity>,
        days: List<RoutineDayEntity>,
        routineExercises: List<RoutineExerciseEntity>,
    ) {
        clearRoutines()
        insertRoutines(routines)
        insertDays(days)
        insertRoutineExercises(routineExercises)
    }
}
