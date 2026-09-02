package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.Exercise

/** Filtro de texto libre sobre el catálogo: nombre, grupo muscular, equipo o tags. */
object ExerciseCatalogSearch {

    fun filter(exercises: List<Exercise>, query: String): List<Exercise> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return exercises
        return exercises.filter { exercise ->
            exercise.name.lowercase().contains(normalized) ||
                exercise.muscleGroup.lowercase().contains(normalized) ||
                exercise.equipment.lowercase().contains(normalized) ||
                exercise.tags.any { it.lowercase().contains(normalized) }
        }
    }
}
