package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.LoggedSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM logged_sets WHERE routineExerciseId = :routineExerciseId")
    fun observeLoggedSets(routineExerciseId: String): Flow<List<LoggedSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loggedSet: LoggedSetEntity)
}
