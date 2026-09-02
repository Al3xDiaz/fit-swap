package com.example.fitswap.domain.model

data class PlannedSet(
    val type: SetType,
    val stageNumber: Int,
    val totalInType: Int,
    val plannedReps: Int,
    val weightFactor: Double,
    val restSeconds: Int,
)
