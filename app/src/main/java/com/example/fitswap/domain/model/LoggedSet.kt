package com.example.fitswap.domain.model

data class LoggedSet(
    val id: String,
    val routineExerciseId: String,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
)
