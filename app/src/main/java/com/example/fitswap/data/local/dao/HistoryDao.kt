package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitswap.data.local.entity.HistoryPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_points WHERE exerciseId = :exerciseId")
    fun observeHistory(exerciseId: String): Flow<List<HistoryPointEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<HistoryPointEntity>)

    @Transaction
    suspend fun replaceAll(points: List<HistoryPointEntity>) {
        clearAll()
        insertAll(points)
    }
}
