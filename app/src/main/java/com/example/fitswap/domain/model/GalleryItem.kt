package com.example.fitswap.domain.model

/** Foto o video agregado desde el picker del sistema, asociado a un [Exercise]. */
data class GalleryItem(
    val id: String,
    val exerciseId: String,
    val uri: String,
    val isVideo: Boolean,
)
