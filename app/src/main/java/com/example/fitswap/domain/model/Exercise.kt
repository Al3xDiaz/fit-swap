package com.example.fitswap.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val tags: List<String> = emptyList(),
)
