package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Sostiene, para la sesión de entrenamiento en curso, qué ejercicio sustituye a cada "ejercicio
 * dentro de rutina" (identificado por [routineExerciseId]) vía el flujo "cambiar ejercicio" — nulo
 * mientras no se haya hecho swap, es decir, se usa el ejercicio original de la rutina.
 */
interface WorkoutSessionRepository {
    fun observeSubstitution(routineExerciseId: String): Flow<Exercise?>
    suspend fun substituteExercise(routineExerciseId: String, exercise: Exercise)
}
