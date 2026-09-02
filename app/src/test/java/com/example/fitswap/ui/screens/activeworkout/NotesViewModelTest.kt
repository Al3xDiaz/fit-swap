package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ROUTINE_ID = "default"
private const val PUSH_DAY_ID = "default-martes"
private const val PRESS_DE_PECHO_ID = "default-martes-press-de-pecho"

class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        exerciseId: String = PRESS_DE_PECHO_ID,
        notesRepository: FakeNotesRepository = FakeNotesRepository(),
        workoutSessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
    ) = NotesViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to PUSH_DAY_ID, "exerciseId" to exerciseId)
        ),
        routineRepository = FakeRoutineRepository(),
        notesRepository = notesRepository,
        workoutSessionRepository = workoutSessionRepository,
    )

    @Test
    fun `expone el nombre del ejercicio y no tiene notas por defecto`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Press de pecho", state.exerciseName)
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `agregar una nota la refleja en el estado`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.addNote("Ajustar asiento a nivel de barbilla")

        val state = viewModel.uiState.first()
        assertEquals(listOf("Ajustar asiento a nivel de barbilla"), state.notes.map { it.text })
    }

    @Test
    fun `una nota en blanco no se agrega`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.addNote("   ")

        val state = viewModel.uiState.first()
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `si el ejercicio fue sustituido, la nota se guarda para el sustituto`() = runTest {
        val notesRepository = FakeNotesRepository()
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(PRESS_DE_PECHO_ID, ExerciseCatalog.pressInclinado)

        val viewModel = viewModel(notesRepository = notesRepository, workoutSessionRepository = sessionRepository)
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals("Press inclinado", state.exerciseName)

        viewModel.addNote("Nota para el sustituto")

        val substituteNotes = notesRepository.observeNotes(ExerciseCatalog.pressInclinado.id).first()
        val originalNotes = notesRepository.observeNotes(ExerciseCatalog.pressDePecho.id).first()
        assertEquals(listOf("Nota para el sustituto"), substituteNotes.map { it.text })
        assertTrue(originalNotes.isEmpty())
    }
}
