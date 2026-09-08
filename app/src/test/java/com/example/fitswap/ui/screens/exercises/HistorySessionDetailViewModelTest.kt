package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// "press-de-pecho" el 2026-08-18 tiene 1 APPROACH + 4 EFFECTIVE (ver FakeHistoryRepository.buildHistorySeed).
private const val EXERCISE_ID = "press-de-pecho"
private val DATE = LocalDate.of(2026, 8, 18)

class HistorySessionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
        notesRepository: FakeNotesRepository = FakeNotesRepository(),
    ) = HistorySessionDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("exerciseId" to EXERCISE_ID, "date" to DATE.toString())),
        exerciseRepository = FakeExerciseRepository(),
        historyRepository = historyRepository,
        notesRepository = notesRepository,
    )

    @Test
    fun `precarga una fila editable por cada punto de esa fecha`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(5, state.rows.size) // 1 approach + 4 effective
        assertEquals(1, state.rows.count { it.type == SetType.APPROACH })
        assertEquals(4, state.rows.count { it.type == SetType.EFFECTIVE })
    }

    @Test
    fun `precarga la nota existente de ese dia`() = runTest {
        val notesRepository = FakeNotesRepository()
        notesRepository.setNote(EXERCISE_ID, DATE, "Buena sesión")
        val viewModel = viewModel(notesRepository = notesRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("Buena sesión", state.noteText)
    }

    @Test
    fun `guardar conserva los ids y persiste los valores editados`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(historyRepository = historyRepository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val firstRow = initial.rows.first()

        viewModel.onWeightChanged(firstRow.id, "65.0")
        viewModel.onRepsChanged(firstRow.id, "6")
        val saved = viewModel.save()

        assertTrue(saved)
        val points = historyRepository.observeHistoryForDate(EXERCISE_ID, DATE).first()
        val updated = points.single { it.id == firstRow.id }
        assertEquals(65.0, updated.weightKg, 0.0)
        assertEquals(6, updated.reps)
        assertEquals(5, points.size) // sigue habiendo la misma cantidad de puntos, solo se editó uno
    }

    @Test
    fun `quitar una fila la borra al guardar sin afectar las demas`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(historyRepository = historyRepository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val toRemove = initial.rows.first()

        viewModel.removeRow(toRemove.id)
        assertEquals(4, viewModel.uiState.first().rows.size)
        val saved = viewModel.save()

        assertTrue(saved)
        val points = historyRepository.observeHistoryForDate(EXERCISE_ID, DATE).first()
        assertEquals(4, points.size)
        assertTrue(points.none { it.id == toRemove.id })
    }

    @Test
    fun `guardar con un reps invalido no persiste nada`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(historyRepository = historyRepository)
        val initial = viewModel.uiState.first { !it.isLoading }
        val firstRow = initial.rows.first()

        viewModel.onRepsChanged(firstRow.id, "no-es-un-numero")
        val saved = viewModel.save()

        assertFalse(saved)
        assertEquals(5, historyRepository.observeHistoryForDate(EXERCISE_ID, DATE).first().size)
    }
}
