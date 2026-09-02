package com.example.fitswap.domain.model

/** Texto libre asociado a un [Exercise] (no a una sesión de entrenamiento específica). */
data class Note(
    val id: String,
    val exerciseId: String,
    val text: String,
)
