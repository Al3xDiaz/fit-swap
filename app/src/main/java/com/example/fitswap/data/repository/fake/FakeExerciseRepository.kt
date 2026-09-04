package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update

@Singleton
class FakeExerciseRepository @Inject constructor() : ExerciseRepository {

    private val exercises = MutableStateFlow(ExerciseCatalog.allExercises)

    override fun observeExercises(): Flow<List<Exercise>> = exercises.asStateFlow()

    override suspend fun getExercise(exerciseId: String): Exercise? =
        exercises.value.find { it.id == exerciseId }

    override suspend fun addExercise(exercise: Exercise) {
        exercises.update { it + exercise }
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exercises.update { list -> list.map { if (it.id == exercise.id) exercise else it } }
    }

    override suspend fun deleteExercise(exerciseId: String): Boolean {
        exercises.update { list -> list.filterNot { it.id == exerciseId } }
        return true
    }
}
