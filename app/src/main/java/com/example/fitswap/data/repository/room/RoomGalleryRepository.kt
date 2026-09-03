package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.GalleryDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.domain.model.GalleryItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomGalleryRepository @Inject constructor(private val galleryDao: GalleryDao) : GalleryRepository {

    override fun observeGallery(exerciseId: String): Flow<List<GalleryItem>> =
        galleryDao.observeGallery(exerciseId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addMedia(exerciseId: String, uri: String, isVideo: Boolean) {
        val id = "media-${System.nanoTime()}"
        galleryDao.insert(GalleryItem(id = id, exerciseId = exerciseId, uri = uri, isVideo = isVideo).toEntity())
    }
}
