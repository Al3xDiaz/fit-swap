package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.domain.model.LoggedSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeSetRepository @Inject constructor() : SetRepository {

    private val loggedSetsByRoutineExercise = MutableStateFlow<Map<String, List<LoggedSet>>>(emptyMap())

    override fun observeLoggedSets(routineExerciseId: String): Flow<List<LoggedSet>> =
        loggedSetsByRoutineExercise.map { it[routineExerciseId].orEmpty() }

    override suspend fun logSet(loggedSet: LoggedSet) {
        loggedSetsByRoutineExercise.update { current ->
            val existing = current[loggedSet.routineExerciseId].orEmpty()
            current + (loggedSet.routineExerciseId to (existing + loggedSet))
        }
    }
}
