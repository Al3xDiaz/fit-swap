package com.example.fitswap.domain.model

data class RoutineExercise(
    val id: String,
    val exercise: Exercise,
    val approachSets: Int,
    val effectiveSets: Int,
    val effectiveRepsLabel: String,
    val approachGuideline: String? = null,
    val restLabel: String,
    val usesStraps: Boolean = false,
    val notes: String? = null,
)

data class RoutineDay(
    val id: String,
    val name: String,
    val exercises: List<RoutineExercise>,
)

data class Routine(
    val id: String,
    val name: String,
    val days: List<RoutineDay>,
    val isDefault: Boolean = false,
)
