package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.BodyProfileEntity
import com.example.fitswap.data.local.entity.SINGLETON_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyProfileDao {
    @Query("SELECT * FROM body_profile WHERE id = $SINGLETON_ID")
    fun observeProfile(): Flow<BodyProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: BodyProfileEntity)
}
