package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercise(exerciseId: String): Exercise?
    suspend fun addExercise(exercise: Exercise)
    suspend fun updateExercise(exercise: Exercise)

    /** Borra el ejercicio y sus datos asociados (galería, notas, historial, sustitutos).
     * Devuelve `false` sin borrar nada si el ejercicio sigue en uso en alguna rutina. */
    suspend fun deleteExercise(exerciseId: String): Boolean
}
