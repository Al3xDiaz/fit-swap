package com.example.fitswap.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.logic.SummaryLogic
import com.example.fitswap.domain.logic.SummaryReport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SummaryUiState(
    val isLoading: Boolean = true,
    val report: SummaryReport = SummaryReport(0, 0, emptyList()),
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.observeAllHistory().collect { allHistory ->
                val report = SummaryLogic.buildReport(allHistory, currentDateProvider.today())
                _uiState.update { it.copy(isLoading = false, report = report) }
            }
        }
    }
}
