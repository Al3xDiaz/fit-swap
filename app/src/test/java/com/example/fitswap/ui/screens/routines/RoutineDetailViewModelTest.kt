package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RoutineDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `expone la rutina indicada por routineId`() = runTest {
        val viewModel = RoutineDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("routineId" to "default")),
            routineRepository = FakeRoutineRepository(),
        )

        val routine = viewModel.routine.first { it != null }

        assertEquals("default", routine?.id)
        assertTrue(routine?.isDefault == true)
    }

    @Test
    fun `eliminar una rutina no default la remueve del repositorio`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = RoutineDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("routineId" to "full-body-rapido")),
            routineRepository = repository,
        )
        viewModel.routine.first { it != null }

        viewModel.deleteRoutine()

        val remaining = repository.observeRoutines().first()
        assertTrue(remaining.none { it.id == "full-body-rapido" })
    }

    @Test
    fun `eliminar la rutina por defecto no tiene efecto`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = RoutineDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("routineId" to "default")),
            routineRepository = repository,
        )
        viewModel.routine.first { it != null }

        viewModel.deleteRoutine()

        val remaining = repository.observeRoutines().first()
        assertTrue(remaining.any { it.id == "default" })
    }
}
