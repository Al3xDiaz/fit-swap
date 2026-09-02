package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSubstituteRepository
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
private const val ELEVACIONES_ID = "default-martes-elevaciones-laterales"

class SwapExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        exerciseId: String,
        workoutSessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
    ) = SwapExerciseViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to PUSH_DAY_ID, "exerciseId" to exerciseId)
        ),
        routineRepository = FakeRoutineRepository(),
        substituteRepository = FakeSubstituteRepository(),
        workoutSessionRepository = workoutSessionRepository,
    )

    @Test
    fun `expone los sustitutos del ejercicio actual del slot`() = runTest {
        val viewModel = viewModel(PRESS_DE_PECHO_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Press de pecho", state.currentExerciseName)
        assertTrue(state.substitutes.any { it.id == ExerciseCatalog.pressInclinado.id })
    }

    @Test
    fun `un ejercicio sin sustitutos curados expone una lista vacia`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(emptyList<Any>(), state.substitutes)
    }

    @Test
    fun `elegir un sustituto lo registra en el repositorio de sesion para ese slot`() = runTest {
        val sessionRepository = FakeWorkoutSessionRepository()
        val viewModel = viewModel(PRESS_DE_PECHO_ID, workoutSessionRepository = sessionRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.selectSubstitute(ExerciseCatalog.pressInclinado)

        val substitution = sessionRepository.observeSubstitution(PRESS_DE_PECHO_ID).first()
        assertEquals(ExerciseCatalog.pressInclinado.id, substitution?.id)
    }
}
