package com.example.fitswap.domain.model

import java.time.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
data class RoutineDay(
    val id: String,
    val name: String,
    val exercises: List<RoutineExercise>,
    /** Nulo para días de rutinas de un solo día no atadas a un día de semana en particular. */
    @Serializable(with = DayOfWeekSerializer::class)
    val dayOfWeek: DayOfWeek? = null,
)

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val days: List<RoutineDay>,
    val isDefault: Boolean = false,
)
