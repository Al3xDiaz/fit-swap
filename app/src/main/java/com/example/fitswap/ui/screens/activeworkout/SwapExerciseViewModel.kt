package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SubstituteRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.domain.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SwapExerciseUiState(
    val isLoading: Boolean = true,
    val currentExerciseName: String = "",
    val substitutes: List<Exercise> = emptyList(),
)

@HiltViewModel
class SwapExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val substituteRepository: SubstituteRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])
    private val dayId: String = checkNotNull(savedStateHandle["dayId"])
    private val routineExerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(SwapExerciseUiState())
    val uiState: StateFlow<SwapExerciseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val routine = routineRepository.observeRoutine(routineId).first { it != null } ?: return@launch
            val day = routine.days.firstOrNull { it.id == dayId } ?: return@launch
            val routineExercise = day.exercises.firstOrNull { it.id == routineExerciseId } ?: return@launch

            val substitution = workoutSessionRepository.observeSubstitution(routineExerciseId).first()
            val currentExercise = substitution ?: routineExercise.exercise
            val substitutes = substituteRepository.substitutesFor(currentExercise.id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentExerciseName = currentExercise.name,
                    substitutes = substitutes,
                )
            }
        }
    }

    fun selectSubstitute(exercise: Exercise) {
        viewModelScope.launch { workoutSessionRepository.substituteExercise(routineExerciseId, exercise) }
    }
}
