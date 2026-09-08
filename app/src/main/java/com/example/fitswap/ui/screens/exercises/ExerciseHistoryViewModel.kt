package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.domain.logic.ExerciseHistoryAnalytics
import com.example.fitswap.domain.logic.HistorySession
import com.example.fitswap.domain.model.CardioSession
import com.example.fitswap.domain.model.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseHistoryUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val exerciseType: ExerciseType = ExerciseType.STRENGTH,
    val maxWeightKg: Double? = null,
    val totalVolumeKg: Double = 0.0,
    val recentSessions: List<HistorySession> = emptyList(),
    val cardioSessions: List<CardioSession> = emptyList(),
    /** Nota de ese día para este ejercicio, si se escribió una (ver [NotesRepository]) —
     * se muestra junto a la sesión correspondiente. */
    val notesByDate: Map<LocalDate, String> = emptyMap(),
)

@HiltViewModel
class ExerciseHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val historyRepository: HistoryRepository,
    private val notesRepository: NotesRepository,
    private val cardioSessionRepository: CardioSessionRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(ExerciseHistoryUiState())
    val uiState: StateFlow<ExerciseHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId)
            _uiState.update { it.copy(exerciseName = exercise?.name.orEmpty(), exerciseType = exercise?.type ?: ExerciseType.STRENGTH) }

            if (exercise?.type == ExerciseType.CARDIO) {
                cardioSessionRepository.observeSessions(exerciseId).collect { sessions ->
                    _uiState.update { it.copy(isLoading = false, cardioSessions = sessions) }
                }
                return@launch
            }

            combine(
                historyRepository.observeHistory(exerciseId),
                notesRepository.observeNotes(exerciseId),
            ) { points, notes ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        maxWeightKg = ExerciseHistoryAnalytics.maxEffectiveWeightKg(points),
                        totalVolumeKg = ExerciseHistoryAnalytics.totalEffectiveVolumeKg(points),
                        recentSessions = ExerciseHistoryAnalytics.recentSessions(points),
                        notesByDate = notes.associate { note -> note.date to note.text },
                    )
                }
            }.collect {}
        }
    }

    fun deleteCardioSession(sessionId: String) {
        viewModelScope.launch { cardioSessionRepository.deleteSession(sessionId) }
    }

    /** Elimina un registro de fuerza completo: todas las series (efectivas y de aproximación) y
     * la nota de ese día. */
    fun deleteHistorySession(date: LocalDate) {
        viewModelScope.launch {
            historyRepository.deleteHistoryForDate(exerciseId, date)
            notesRepository.deleteNote(exerciseId, date)
        }
    }
}
