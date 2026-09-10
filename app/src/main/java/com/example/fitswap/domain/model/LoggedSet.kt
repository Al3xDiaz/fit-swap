package com.example.fitswap.domain.model

import java.time.LocalDate

data class LoggedSet(
    val id: String,
    val routineExerciseId: String,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
    val date: LocalDate,
)
