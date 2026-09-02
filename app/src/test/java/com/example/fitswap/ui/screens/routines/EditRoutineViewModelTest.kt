package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.MoveDirection
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val PUSH_DAY_ID = "default-martes"

class EditRoutineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(routineId: String, repository: FakeRoutineRepository = FakeRoutineRepository()) =
        EditRoutineViewModel(
            savedStateHandle = SavedStateHandle(mapOf("routineId" to routineId)),
            routineRepository = repository,
        )

    @Test
    fun `una rutina existente carga directo, sin pedir nombre`() = runTest {
        val viewModel = viewModel("default")

        val state = viewModel.uiState.first { !it.isLoading }

        assertTrue(!state.needsName)
        assertEquals("default", state.routine?.id)
    }

    @Test
    fun `una rutina nueva pide nombre antes de crearla`() = runTest {
        val viewModel = viewModel("new")

        val state = viewModel.uiState.first { !it.isLoading }

        assertTrue(state.needsName)
        assertEquals(null, state.routine)
    }

    @Test
    fun `crear la rutina nueva deja de pedir nombre y expone la rutina creada`() = runTest {
        val viewModel = viewModel("new")
        viewModel.uiState.first { !it.isLoading }

        viewModel.createRoutine("Mi rutina")

        val state = viewModel.uiState.first { it.routine != null }
        assertTrue(!state.needsName)
        assertEquals("Mi rutina", state.routine?.name)
        assertTrue(state.routine?.days?.isEmpty() == true)
    }

    @Test
    fun `agregar un dia a la rutina recien creada se refleja en el estado`() = runTest {
        val viewModel = viewModel("new")
        viewModel.uiState.first { !it.isLoading }
        viewModel.createRoutine("Mi rutina")
        viewModel.uiState.first { it.routine != null }

        viewModel.addDay("Día único")

        val state = viewModel.uiState.first { it.routine?.days?.isNotEmpty() == true }
        assertEquals(listOf("Día único"), state.routine?.days?.map { it.name })
    }

    @Test
    fun `agregar y luego mover un ejercicio se refleja en el orden expuesto`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = viewModel("default", repository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val initialExercises = initial.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises

        viewModel.addExercise(PUSH_DAY_ID, ExerciseCatalog.caminarEnCinta.id)
        val afterAdd = viewModel.uiState.first {
            it.routine!!.days.first { day -> day.id == PUSH_DAY_ID }.exercises.size == initialExercises.size + 1
        }
        val addedId = afterAdd.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises.last().id

        viewModel.moveExercise(PUSH_DAY_ID, addedId, MoveDirection.UP)

        val afterMove = viewModel.uiState.first {
            val exercises = it.routine!!.days.first { day -> day.id == PUSH_DAY_ID }.exercises
            exercises.getOrNull(exercises.size - 2)?.id == addedId
        }
        val exercises = afterMove.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises
        assertEquals(addedId, exercises[exercises.size - 2].id)
    }

    @Test
    fun `beginPickingExerciseFor recuerda el dia pendiente aunque la pantalla se recomponga entre medio`() = runTest {
        // Regresión: el día pendiente vivía en `remember` de la pantalla, que Navigation Compose
        // descompone mientras el catálogo (modo elegir) está arriba en el backstack — por eso este
        // estado tiene que vivir en el ViewModel, no en la Composable. Ver EditRoutineScreen.kt.
        val repository = FakeRoutineRepository()
        val viewModel = viewModel("default", repository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val initialCount = initial.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises.size

        viewModel.beginPickingExerciseFor(PUSH_DAY_ID)
        viewModel.onExercisePicked(ExerciseCatalog.caminarEnCinta.id)

        val state = viewModel.uiState.first {
            it.routine!!.days.first { day -> day.id == PUSH_DAY_ID }.exercises.size == initialCount + 1
        }
        val added = state.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises.last()
        assertEquals(ExerciseCatalog.caminarEnCinta, added.exercise)
    }

    @Test
    fun `onExercisePicked sin haber elegido un dia antes no hace nada`() = runTest {
        val repository = FakeRoutineRepository()
        val viewModel = viewModel("default", repository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val initialCount = initial.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises.size

        viewModel.onExercisePicked(ExerciseCatalog.caminarEnCinta.id)

        val state = viewModel.uiState.first()
        assertEquals(initialCount, state.routine!!.days.first { it.id == PUSH_DAY_ID }.exercises.size)
    }
}
