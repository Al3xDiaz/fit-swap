package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.domain.model.LoggedSet
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeSetRepository @Inject constructor() : SetRepository {

    private val loggedSetsByRoutineExercise = MutableStateFlow<Map<String, List<LoggedSet>>>(emptyMap())

    override fun observeLoggedSets(routineExerciseId: String, date: LocalDate): Flow<List<LoggedSet>> =
        loggedSetsByRoutineExercise.map { it[routineExerciseId].orEmpty().filter { set -> set.date == date } }

    override suspend fun logSet(loggedSet: LoggedSet) {
        loggedSetsByRoutineExercise.update { current ->
            val existing = current[loggedSet.routineExerciseId].orEmpty()
            current + (loggedSet.routineExerciseId to (existing + loggedSet))
        }
    }

    override suspend fun deleteLoggedSetsForDate(routineExerciseId: String, date: LocalDate) {
        loggedSetsByRoutineExercise.update { current ->
            val existing = current[routineExerciseId].orEmpty().filterNot { it.date == date }
            current + (routineExerciseId to existing)
        }
    }

    override suspend fun deleteAllLoggedSetsForDate(date: LocalDate) {
        loggedSetsByRoutineExercise.update { current ->
            current.mapValues { (_, sets) -> sets.filterNot { it.date == date } }
        }
    }
}
