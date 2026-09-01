package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeRoutineRepository @Inject constructor() : RoutineRepository {

    private val routines = MutableStateFlow(seedRoutines())

    override fun observeRoutines(): Flow<List<Routine>> = routines.asStateFlow()

    override fun observeRoutine(routineId: String): Flow<Routine?> =
        routines.map { list -> list.find { it.id == routineId } }

    override suspend fun deleteRoutine(routineId: String) {
        routines.update { list -> list.filterNot { it.id == routineId && !it.isDefault } }
    }
}
