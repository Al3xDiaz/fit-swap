package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.domain.model.GalleryItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeGalleryRepository @Inject constructor() : GalleryRepository {

    private val itemsByExercise = MutableStateFlow<Map<String, List<GalleryItem>>>(emptyMap())
    private var itemSeq = 0

    override fun observeGallery(exerciseId: String): Flow<List<GalleryItem>> =
        itemsByExercise.map { it[exerciseId].orEmpty() }

    override suspend fun addMedia(exerciseId: String, uri: String, isVideo: Boolean) {
        val item = GalleryItem(id = "media-${itemSeq++}", exerciseId = exerciseId, uri = uri, isVideo = isVideo)
        itemsByExercise.update { current ->
            val existing = current[exerciseId].orEmpty()
            current + (exerciseId to (existing + item))
        }
    }
}
