package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.GalleryItem
import kotlinx.coroutines.flow.Flow

interface GalleryRepository {
    fun observeGallery(exerciseId: String): Flow<List<GalleryItem>>
    suspend fun addMedia(exerciseId: String, uri: String, isVideo: Boolean)
}
