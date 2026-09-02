package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercise(exerciseId: String): Exercise?
}
