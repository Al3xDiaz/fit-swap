package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.GalleryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Query("SELECT * FROM gallery_items WHERE exerciseId = :exerciseId")
    fun observeGallery(exerciseId: String): Flow<List<GalleryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GalleryItemEntity)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM gallery_items WHERE exerciseId = :exerciseId")
    suspend fun deleteAllForExercise(exerciseId: String)
}
