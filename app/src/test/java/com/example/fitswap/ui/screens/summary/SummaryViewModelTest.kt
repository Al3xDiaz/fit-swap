package com.example.fitswap.ui.screens.summary

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `expone un reporte calculado sobre el historial de todos los ejercicios`() = runTest {
        val viewModel = SummaryViewModel(
            historyRepository = FakeHistoryRepository(),
            currentDateProvider = FixedCurrentDateProvider(DayOfWeek.TUESDAY),
        )

        val state = viewModel.uiState.first { !it.isLoading }

        // FakeHistoryRepository trae historial sembrado de agosto 2026 para 2 ejercicios.
        assertTrue(state.report.monthlyVolumes.isNotEmpty())
    }
}
