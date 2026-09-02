package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class FakeExerciseRepository @Inject constructor() : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> = flowOf(ExerciseCatalog.allExercises)

    override suspend fun getExercise(exerciseId: String): Exercise? =
        ExerciseCatalog.allExercises.find { it.id == exerciseId }
}
