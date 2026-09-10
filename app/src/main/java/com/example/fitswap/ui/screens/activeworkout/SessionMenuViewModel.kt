package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.session.RoutineSessionFinisher
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.logic.SetPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SessionExerciseStatus { DONE, ACTIVE, PENDING }

data class SessionMenuItem(
    val routineExerciseId: String,
    val exerciseName: String,
    val status: SessionExerciseStatus,
)

data class SessionMenuUiState(
    val isLoading: Boolean = true,
    val dayName: String = "",
    val items: List<SessionMenuItem> = emptyList(),
)

@HiltViewModel
class SessionMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val setRepository: SetRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val currentDateProvider: CurrentDateProvider,
    private val routineSessionFinisher: RoutineSessionFinisher,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])
    private val dayId: String = checkNotNull(savedStateHandle["dayId"])
    private val activeRoutineExerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(SessionMenuUiState())
    val uiState: StateFlow<SessionMenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val routine = routineRepository.observeRoutine(routineId).first { it != null } ?: return@launch
            val day = routine.days.firstOrNull { it.id == dayId } ?: return@launch

            val items = day.exercises.map { routineExercise ->
                val plan = SetPlanner.buildPlan(routineExercise)
                val loggedCount = setRepository.observeLoggedSets(routineExercise.id, currentDateProvider.today()).first().size
                val substitution = workoutSessionRepository.observeSubstitution(routineExercise.id).first()
                val status = when {
                    routineExercise.id == activeRoutineExerciseId -> SessionExerciseStatus.ACTIVE
                    loggedCount >= plan.size -> SessionExerciseStatus.DONE
                    else -> SessionExerciseStatus.PENDING
                }
                SessionMenuItem(
                    routineExerciseId = routineExercise.id,
                    exerciseName = substitution?.name ?: routineExercise.exercise.name,
                    status = status,
                )
            }

            _uiState.update { it.copy(isLoading = false, dayName = day.name, items = items) }
        }
    }

    /** Conserva todo lo registrado hoy — ver [RoutineSessionFinisher.saveAndFinish]. */
    fun saveRoutine() {
        viewModelScope.launch { routineSessionFinisher.saveAndFinish() }
    }

    /** Borra todo lo registrado hoy en la rutina — ver [RoutineSessionFinisher.discardAndFinish]. */
    fun discardRoutine() {
        viewModelScope.launch { routineSessionFinisher.discardAndFinish() }
    }
}
