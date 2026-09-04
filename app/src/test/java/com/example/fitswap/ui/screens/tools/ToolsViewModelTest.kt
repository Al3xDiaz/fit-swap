package com.example.fitswap.ui.screens.tools

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSettingsRepository
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ToolsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        routineRepository: FakeRoutineRepository = FakeRoutineRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
        notesRepository: FakeNotesRepository = FakeNotesRepository(),
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
    ) = ToolsViewModel(routineRepository, historyRepository, notesRepository, settingsRepository)

    @Test
    fun `exportar y volver a importar reproduce el mismo estado en repos vacios`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val historyRepository = FakeHistoryRepository()
        val notesRepository = FakeNotesRepository()
        val settingsRepository = FakeSettingsRepository()
        settingsRepository.updateTheme(AppTheme.DARK)
        notesRepository.setNote("press-de-pecho", LocalDate.of(2026, 9, 1), "nota de prueba")

        val viewModel = viewModel(routineRepository, historyRepository, notesRepository, settingsRepository)

        val exportedJson = viewModel.buildExportJson()
        val routinesBeforeReset = routineRepository.observeRoutines().first()
        val historyBeforeReset = historyRepository.observeAllHistory().first()
        val notesBeforeReset = notesRepository.observeAllNotes().first()

        // Simula una reinstalación: repos vacíos antes de reimportar el backup.
        routineRepository.replaceRoutines(emptyList())
        historyRepository.replaceAllHistory(emptyList())
        notesRepository.replaceAllNotes(emptyList())
        settingsRepository.replaceSettings(AppSettings())

        viewModel.importFromJson(exportedJson)

        assertEquals(routinesBeforeReset, routineRepository.observeRoutines().first())
        assertEquals(historyBeforeReset, historyRepository.observeAllHistory().first())
        assertEquals(notesBeforeReset, notesRepository.observeAllNotes().first())
        assertEquals(AppTheme.DARK, settingsRepository.observeSettings().first().theme)
        assertEquals("Importación completa", viewModel.uiState.first().message)
    }

    @Test
    fun `importar un json invalido expone un error y no toca los repos`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val viewModel = viewModel(routineRepository = routineRepository)
        val routinesBefore = routineRepository.observeRoutines().first()

        viewModel.importFromJson("esto no es json")

        assertEquals(routinesBefore, routineRepository.observeRoutines().first())
        assertTrue(viewModel.uiState.first().message?.startsWith("Error al importar") == true)
    }
}
