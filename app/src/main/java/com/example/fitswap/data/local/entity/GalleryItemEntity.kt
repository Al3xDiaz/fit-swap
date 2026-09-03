package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.GalleryItem

@Entity(
    tableName = "gallery_items",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("exerciseId")],
)
data class GalleryItemEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val uri: String,
    val isVideo: Boolean,
)

fun GalleryItemEntity.toDomain(): GalleryItem = GalleryItem(id = id, exerciseId = exerciseId, uri = uri, isVideo = isVideo)

fun GalleryItem.toEntity(): GalleryItemEntity = GalleryItemEntity(id = id, exerciseId = exerciseId, uri = uri, isVideo = isVideo)
