package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.LoggedSet
import kotlinx.coroutines.flow.Flow

interface SetRepository {
    fun observeLoggedSets(routineExerciseId: String): Flow<List<LoggedSet>>
    suspend fun logSet(loggedSet: LoggedSet)
}
