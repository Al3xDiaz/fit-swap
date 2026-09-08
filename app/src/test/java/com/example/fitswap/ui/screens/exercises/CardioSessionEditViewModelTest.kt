package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeCardioSessionRepository
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val EXERCISE_ID = ExerciseCatalog.caminarEnCinta.id
private const val SESSION_ID = "session-1"

class CardioSessionEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(cardioSessionRepository: FakeCardioSessionRepository) = CardioSessionEditViewModel(
        savedStateHandle = SavedStateHandle(mapOf("exerciseId" to EXERCISE_ID, "sessionId" to SESSION_ID)),
        exerciseRepository = FakeExerciseRepository(),
        cardioSessionRepository = cardioSessionRepository,
    )

    @Test
    fun `precarga los campos con los datos de la sesion existente`() = runTest {
        val cardioSessionRepository = FakeCardioSessionRepository()
        cardioSessionRepository.addSession(
            CardioSession(
                id = SESSION_ID, exerciseId = EXERCISE_ID, date = LocalDate.of(2026, 9, 1),
                durationSeconds = 600, distanceKm = 5.0, avgHeartRate = 140, calories = 300,
            )
        )
        val viewModel = viewModel(cardioSessionRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("10", state.durationMinutes)
        assertEquals("5", state.distanceKm)
        assertEquals("140", state.avgHeartRate)
        assertEquals("300", state.calories)
    }

    @Test
    fun `guardar conserva el id y la fecha originales, con los campos editados`() = runTest {
        val cardioSessionRepository = FakeCardioSessionRepository()
        cardioSessionRepository.addSession(
            CardioSession(
                id = SESSION_ID, exerciseId = EXERCISE_ID, date = LocalDate.of(2026, 9, 1),
                durationSeconds = 600, distanceKm = 5.0, avgHeartRate = 140, calories = 300,
            )
        )
        val viewModel = viewModel(cardioSessionRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onDurationChanged("15")
        viewModel.onDistanceChanged("7.5")
        val saved = viewModel.save()

        assertTrue(saved)
        val session = cardioSessionRepository.observeSessions(EXERCISE_ID).first().single()
        assertEquals(SESSION_ID, session.id)
        assertEquals(LocalDate.of(2026, 9, 1), session.date)
        assertEquals(900, session.durationSeconds)
        assertEquals(7.5, session.distanceKm)
    }

    @Test
    fun `guardar sin duracion no persiste nada`() = runTest {
        val cardioSessionRepository = FakeCardioSessionRepository()
        cardioSessionRepository.addSession(
            CardioSession(
                id = SESSION_ID, exerciseId = EXERCISE_ID, date = LocalDate.of(2026, 9, 1),
                durationSeconds = 600, distanceKm = null, avgHeartRate = null, calories = null,
            )
        )
        val viewModel = viewModel(cardioSessionRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onDurationChanged("")
        val saved = viewModel.save()

        assertFalse(saved)
        assertEquals(600, cardioSessionRepository.observeSessions(EXERCISE_ID).first().single().durationSeconds)
    }
}
