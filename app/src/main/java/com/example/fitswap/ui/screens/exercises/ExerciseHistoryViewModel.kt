package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.domain.logic.ExerciseHistoryAnalytics
import com.example.fitswap.domain.logic.HistorySession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseHistoryUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val maxWeightKg: Double? = null,
    val totalVolumeKg: Double = 0.0,
    val recentSessions: List<HistorySession> = emptyList(),
)

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(ExerciseHistoryUiState())
    val uiState: StateFlow<ExerciseHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId)
            historyRepository.observeHistory(exerciseId).collect { points ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exerciseName = exercise?.name.orEmpty(),
                        maxWeightKg = ExerciseHistoryAnalytics.maxEffectiveWeightKg(points),
                        totalVolumeKg = ExerciseHistoryAnalytics.totalEffectiveVolumeKg(points),
                        recentSessions = ExerciseHistoryAnalytics.recentSessions(points),
                    )
                }
            }
        }
    }
}
