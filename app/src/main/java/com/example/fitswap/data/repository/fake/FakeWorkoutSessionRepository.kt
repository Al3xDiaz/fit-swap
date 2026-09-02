package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeWorkoutSessionRepository @Inject constructor() : WorkoutSessionRepository {

    private val substitutionsByRoutineExercise = MutableStateFlow<Map<String, Exercise>>(emptyMap())

    override fun observeSubstitution(routineExerciseId: String): Flow<Exercise?> =
        substitutionsByRoutineExercise.map { it[routineExerciseId] }

    override suspend fun substituteExercise(routineExerciseId: String, exercise: Exercise) {
        substitutionsByRoutineExercise.update { it + (routineExerciseId to exercise) }
    }
}
