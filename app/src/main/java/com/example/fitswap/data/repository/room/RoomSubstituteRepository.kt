package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.SubstituteDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.repository.SubstituteRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSubstituteRepository @Inject constructor(private val substituteDao: SubstituteDao) : SubstituteRepository {

    override suspend fun substitutesFor(exerciseId: String): List<Exercise> {
        val groupIds = substituteDao.groupsFor(exerciseId)
        if (groupIds.isEmpty()) return emptyList()
        return substituteDao.exercisesInGroups(groupIds, excludingExerciseId = exerciseId).map { it.toDomain() }
    }
}
