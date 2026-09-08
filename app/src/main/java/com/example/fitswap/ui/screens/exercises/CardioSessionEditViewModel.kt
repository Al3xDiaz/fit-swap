package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.model.CardioSession
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardioSessionEditUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val durationMinutes: String = "",
    val distanceKm: String = "",
    val avgHeartRate: String = "",
    val calories: String = "",
) {
    val canSave: Boolean get() = durationMinutes.toDoubleOrNull() != null
}

/** Edita una [CardioSession] ya guardada desde el historial. La fecha no es editable — se conserva
 * la de la sesión original; solo los 4 campos de entrada manual cambian. */
@HiltViewModel
class CardioSessionEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val cardioSessionRepository: CardioSessionRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])
    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])
    private var originalDate: LocalDate = LocalDate.now()

    private val _uiState = MutableStateFlow(CardioSessionEditUiState())
    val uiState: StateFlow<CardioSessionEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId)
            val session = cardioSessionRepository.observeSessions(exerciseId).first().find { it.id == sessionId }
            if (session != null) originalDate = session.date

            _uiState.update {
                it.copy(
                    isLoading = false,
                    exerciseName = exercise?.name.orEmpty(),
                    durationMinutes = session?.let { s -> formatMinutes(s.durationSeconds) }.orEmpty(),
                    distanceKm = session?.distanceKm?.let(::formatNumber).orEmpty(),
                    avgHeartRate = session?.avgHeartRate?.toString().orEmpty(),
                    calories = session?.calories?.toString().orEmpty(),
                )
            }
        }
    }

    fun onDurationChanged(value: String) {
        _uiState.update { it.copy(durationMinutes = value) }
    }

    fun onDistanceChanged(value: String) {
        _uiState.update { it.copy(distanceKm = value) }
    }

    fun onHeartRateChanged(value: String) {
        _uiState.update { it.copy(avgHeartRate = value) }
    }

    fun onCaloriesChanged(value: String) {
        _uiState.update { it.copy(calories = value) }
    }

    /** Devuelve `false` sin persistir nada si falta el único campo obligatorio (duración). */
    suspend fun save(): Boolean {
        val state = _uiState.value
        val minutes = state.durationMinutes.toDoubleOrNull() ?: return false

        cardioSessionRepository.updateSession(
            CardioSession(
                id = sessionId,
                exerciseId = exerciseId,
                date = originalDate,
                durationSeconds = (minutes * 60).roundToInt(),
                distanceKm = state.distanceKm.toDoubleOrNull(),
                avgHeartRate = state.avgHeartRate.toIntOrNull(),
                calories = state.calories.toIntOrNull(),
            )
        )
        return true
    }
}

private fun formatMinutes(durationSeconds: Int): String = formatNumber(durationSeconds / 60.0)

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.1f".format(value)
