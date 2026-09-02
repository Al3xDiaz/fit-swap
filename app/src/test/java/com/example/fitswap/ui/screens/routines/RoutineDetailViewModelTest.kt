package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RoutineDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        routineId: String = "default",
        today: DayOfWeek = DayOfWeek.TUESDAY,
        routineRepository: FakeRoutineRepository = FakeRoutineRepository(),
    ) = RoutineDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("routineId" to routineId)),
        routineRepository = routineRepository,
        currentDateProvider = FixedCurrentDateProvider(today),
    )

    @Test
    fun `expone la rutina indicada por routineId`() = runTest {
        val viewModel = viewModel()

        val routine = viewModel.routine.first { it != null }

        assertEquals("default", routine?.id)
        assertTrue(routine?.isDefault == true)
    }

    @Test
    fun `eliminar una rutina no default la remueve del repositorio`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = viewModel(routineId = "full-body-rapido", routineRepository = repository)
        viewModel.routine.first { it != null }

        viewModel.deleteRoutine()

        val remaining = repository.observeRoutines().first()
        assertTrue(remaining.none { it.id == "full-body-rapido" })
    }

    @Test
    fun `eliminar la rutina por defecto no tiene efecto`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = viewModel(routineRepository = repository)
        viewModel.routine.first { it != null }

        viewModel.deleteRoutine()

        val remaining = repository.observeRoutines().first()
        assertTrue(remaining.any { it.id == "default" })
    }

    @Test
    fun `el dia seleccionado por defecto es el de hoy si la rutina tiene un dia para esa fecha`() = runTest {
        // "Miércoles" en el seed de la rutina por defecto -> índice 1 (Pull).
        val viewModel = viewModel(today = DayOfWeek.WEDNESDAY)

        viewModel.routine.first { it != null }

        assertEquals(1, viewModel.selectedDayIndex.value)
    }

    @Test
    fun `el dia seleccionado por defecto es el primero si hoy no tiene entrenamiento programado`() = runTest {
        // Lunes es "Descanso" en rutina_semanal_optimizada.md, no está en la rutina por defecto.
        val viewModel = viewModel(today = DayOfWeek.MONDAY)

        viewModel.routine.first { it != null }

        assertEquals(0, viewModel.selectedDayIndex.value)
    }

    @Test
    fun `selectDay cambia al indice pedido`() = runTest {
        val viewModel = viewModel(today = DayOfWeek.TUESDAY) // índice 0 por defecto
        viewModel.routine.first { it != null }

        viewModel.selectDay(3) // Sábado — Full Upper

        assertEquals(3, viewModel.selectedDayIndex.value)
    }

    @Test
    fun `selectDay con un indice fuera de rango no hace nada`() = runTest {
        val viewModel = viewModel(today = DayOfWeek.TUESDAY) // índice 0 por defecto
        viewModel.routine.first { it != null }

        viewModel.selectDay(99)

        assertEquals(0, viewModel.selectedDayIndex.value)
    }
}
