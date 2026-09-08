package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeCardioSessionRepository
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun viewModel(
    exerciseId: String,
    cardioSessionRepository: FakeCardioSessionRepository = FakeCardioSessionRepository(),
) = ExerciseHistoryViewModel(
    savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId)),
    exerciseRepository = FakeExerciseRepository(),
    historyRepository = FakeHistoryRepository(),
    notesRepository = FakeNotesRepository(),
    cardioSessionRepository = cardioSessionRepository,
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

    @Test
    fun `deleteCardioSession la quita del historial de cardio`() = runTest {
        val cardioSessionRepository = FakeCardioSessionRepository()
        cardioSessionRepository.addSession(
            CardioSession(
                id = "session-1", exerciseId = ExerciseCatalog.caminarEnCinta.id, date = LocalDate.of(2026, 9, 1),
                durationSeconds = 600, distanceKm = 5.0, avgHeartRate = 140, calories = 300,
            )
        )
        val viewModel = viewModel(ExerciseCatalog.caminarEnCinta.id, cardioSessionRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.deleteCardioSession("session-1")

        val sessions = cardioSessionRepository.observeSessions(ExerciseCatalog.caminarEnCinta.id).first()
        assertTrue(sessions.isEmpty())
    }
}
