package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.domain.logic.SetPlanner
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.PlannedSet
import com.example.fitswap.domain.model.RoutineExercise
import com.example.fitswap.domain.model.SetType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveExerciseUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null,
    val phases: List<PhaseStatus> = emptyList(),
    val effectiveWeightKg: Double = 1.0,
    val lastLoggedWeightKg: Double? = null,
    val currentSet: PlannedSet? = null,
    val currentStageWeightKg: Double = 1.0,
    val currentStageWeightIsEffective: Boolean = false,
    val currentReps: Int = 0,
    val effectiveSetsDone: Int = 0,
    val effectiveSetsTotal: Int = 0,
    val restRemainingSeconds: Int? = null,
    val restTotalSeconds: Int = 0,
    val isComplete: Boolean = false,
)

data class PhaseStatus(val type: SetType, val state: PhaseState)

enum class PhaseState { DONE, ACTIVE, PENDING }

@HiltViewModel
class ActiveExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val setRepository: SetRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])
    private val dayId: String = checkNotNull(savedStateHandle["dayId"])
    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(ActiveExerciseUiState())
    val uiState: StateFlow<ActiveExerciseUiState> = _uiState.asStateFlow()

    private var routineExercise: RoutineExercise? = null
    private var plan: List<PlannedSet> = emptyList()
    private var planIndex = 0
    private var loggedSetSeq = 0
    private var effectiveWeightKg = 1.0
    private var manualStageWeightOverrideKg: Double? = null
    private var restJob: Job? = null

    init {
        viewModelScope.launch {
            val routine = routineRepository.observeRoutine(routineId).first { it != null } ?: return@launch
            val day = routine.days.firstOrNull { it.id == dayId } ?: return@launch
            val exercise = day.exercises.firstOrNull { it.id == exerciseId } ?: return@launch

            routineExercise = exercise
            plan = SetPlanner.buildPlan(exercise)

            val lastWeight = historyRepository.lastWeightKg(exercise.exercise.id)
            effectiveWeightKg = SetPlanner.suggestedEffectiveWeight(lastWeight)

            val existingLogged = setRepository.observeLoggedSets(exercise.id).first()
            planIndex = existingLogged.size.coerceAtMost(plan.size)
            loggedSetSeq = existingLogged.size

            _uiState.update { it.copy(lastLoggedWeightKg = lastWeight) }
            refreshUiState()
        }
    }

    fun updateEffectiveWeight(newWeightKg: Double) {
        effectiveWeightKg = newWeightKg.coerceAtLeast(0.0)
        refreshUiState()
    }

    fun updateCurrentStageWeight(newWeightKg: Double) {
        if (_uiState.value.currentStageWeightIsEffective) {
            updateEffectiveWeight(newWeightKg)
        } else {
            manualStageWeightOverrideKg = newWeightKg.coerceAtLeast(0.0)
            refreshUiState()
        }
    }

    fun incrementReps() {
        _uiState.update { it.copy(currentReps = it.currentReps + 1) }
    }

    fun decrementReps() {
        _uiState.update { it.copy(currentReps = (it.currentReps - 1).coerceAtLeast(0)) }
    }

    fun registerSet() {
        val exercise = routineExercise ?: return
        val current = plan.getOrNull(planIndex) ?: return
        val state = _uiState.value

        val loggedSet = LoggedSet(
            id = "${exercise.id}-${loggedSetSeq++}",
            routineExerciseId = exercise.id,
            type = current.type,
            reps = state.currentReps,
            weightKg = state.currentStageWeightKg,
        )
        viewModelScope.launch { setRepository.logSet(loggedSet) }

        planIndex++
        manualStageWeightOverrideKg = null
        startRestTimer(current.restSeconds)
        refreshUiState()
    }

    private fun startRestTimer(totalSeconds: Int) {
        restJob?.cancel()
        restJob = viewModelScope.launch {
            var remaining = totalSeconds
            _uiState.update { it.copy(restRemainingSeconds = remaining, restTotalSeconds = totalSeconds) }
            while (remaining > 0) {
                delay(1_000)
                remaining--
                _uiState.update { it.copy(restRemainingSeconds = remaining) }
            }
            _uiState.update { it.copy(restRemainingSeconds = null) }
        }
    }

    private fun refreshUiState() {
        val exercise = routineExercise ?: return
        val current = plan.getOrNull(planIndex)
        val stageWeightKg = when {
            current == null -> effectiveWeightKg
            current.type == SetType.EFFECTIVE -> effectiveWeightKg
            else -> manualStageWeightOverrideKg ?: (current.weightFactor * effectiveWeightKg)
        }
        val effectiveSetsDone = plan.take(planIndex).count { it.type == SetType.EFFECTIVE }
        val effectiveSetsTotal = plan.count { it.type == SetType.EFFECTIVE }

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                exercise = exercise.exercise,
                phases = buildPhaseStatuses(current?.type),
                effectiveWeightKg = effectiveWeightKg,
                currentSet = current,
                currentStageWeightKg = stageWeightKg,
                currentStageWeightIsEffective = current?.type == SetType.EFFECTIVE,
                currentReps = current?.plannedReps ?: state.currentReps,
                effectiveSetsDone = effectiveSetsDone,
                effectiveSetsTotal = effectiveSetsTotal,
                isComplete = current == null,
            )
        }
    }

    private fun buildPhaseStatuses(currentType: SetType?): List<PhaseStatus> =
        SetType.entries.map { type ->
            val itemsOfType = plan.filter { it.type == type }
            val lastIndexOfType = plan.indexOfLast { it.type == type }
            val state = when {
                type == currentType -> PhaseState.ACTIVE
                itemsOfType.isEmpty() || lastIndexOfType < planIndex -> PhaseState.DONE
                else -> PhaseState.PENDING
            }
            PhaseStatus(type, state)
        }
}
