package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.ExerciseDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomExerciseRepository @Inject constructor(private val exerciseDao: ExerciseDao) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeExercises().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getExercise(exerciseId: String): Exercise? =
        exerciseDao.getExercise(exerciseId)?.toDomain()
}
