package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun viewModel(exerciseId: String) = ExerciseHistoryViewModel(
    savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId)),
    exerciseRepository = FakeExerciseRepository(),
    historyRepository = FakeHistoryRepository(),
)

class ExerciseHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `expone el nombre del ejercicio y sus metricas cuando hay historial sembrado`() = runTest {
        val viewModel = viewModel("press-de-pecho")

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Press de pecho", state.exerciseName)
        assertEquals(60.0, state.maxWeightKg)
        assertEquals(3, state.recentSessions.size)
    }

    @Test
    fun `un ejercicio sin historial expone una lista de sesiones vacia`() = runTest {
        val viewModel = viewModel("elevaciones-laterales")

        val state = viewModel.uiState.first { !it.isLoading }

        assertNull(state.maxWeightKg)
        assertTrue(state.recentSessions.isEmpty())
    }
}
