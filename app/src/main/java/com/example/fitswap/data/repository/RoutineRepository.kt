package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>
    fun observeRoutine(routineId: String): Flow<Routine?>
    suspend fun deleteRoutine(routineId: String)
}
