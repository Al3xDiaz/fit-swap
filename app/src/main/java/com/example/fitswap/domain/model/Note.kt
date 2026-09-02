package com.example.fitswap.domain.model

import kotlinx.serialization.Serializable

/** Texto libre asociado a un [Exercise] (no a una sesión de entrenamiento específica). */
@Serializable
data class Note(
    val id: String,
    val exerciseId: String,
    val text: String,
)
