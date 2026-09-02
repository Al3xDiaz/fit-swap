package com.example.fitswap.domain.model

import java.time.LocalDate

/** Un punto de historial: una serie registrada en el pasado para un [Exercise]. */
data class HistoryPoint(
    val id: String,
    val exerciseId: String,
    val date: LocalDate,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
)
