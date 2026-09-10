package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.SetDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.domain.model.LoggedSet
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomSetRepository @Inject constructor(private val setDao: SetDao) : SetRepository {

    override fun observeLoggedSets(routineExerciseId: String, date: LocalDate): Flow<List<LoggedSet>> =
        setDao.observeLoggedSets(routineExerciseId, date).map { entities -> entities.map { it.toDomain() } }

    override suspend fun logSet(loggedSet: LoggedSet) {
        setDao.insert(loggedSet.toEntity())
    }

    override suspend fun deleteSets(ids: List<String>) {
        if (ids.isNotEmpty()) setDao.deleteByIds(ids)
    }

    override suspend fun deleteLoggedSetsForDate(routineExerciseId: String, date: LocalDate) {
        setDao.deleteLoggedSetsForDate(routineExerciseId, date)
    }

    override suspend fun deleteAllLoggedSetsForDate(date: LocalDate) {
        setDao.deleteAllLoggedSetsForDate(date)
    }
}
