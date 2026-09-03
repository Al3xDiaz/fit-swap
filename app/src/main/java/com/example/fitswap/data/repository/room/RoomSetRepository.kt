package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.SetDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.domain.model.LoggedSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomSetRepository @Inject constructor(private val setDao: SetDao) : SetRepository {

    override fun observeLoggedSets(routineExerciseId: String): Flow<List<LoggedSet>> =
        setDao.observeLoggedSets(routineExerciseId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun logSet(loggedSet: LoggedSet) {
        setDao.insert(loggedSet.toEntity())
    }
}
