package com.example.fitswap.domain.model

import java.time.LocalDate
import kotlinx.serialization.Serializable

/** Un punto de historial: una serie registrada en el pasado para un [Exercise]. */
@Serializable
data class HistoryPoint(
    val id: String,
    val exerciseId: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
)
