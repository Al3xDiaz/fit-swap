package com.example.fitswap.ui.screens.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.MoveDirection
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Debe coincidir con `NEW_ROUTINE_ID` en `RoutineNavigation.kt` — sentinel de "rutina nueva, aún no creada". */
private const val NEW_ROUTINE_SENTINEL = "new"

data class EditRoutineUiState(
    val isLoading: Boolean = true,
    val needsName: Boolean = false,
    val routine: Routine? = null,
)

@HiltViewModel
class EditRoutineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val initialRoutineId: String = checkNotNull(savedStateHandle["routineId"])

    private val _uiState = MutableStateFlow(EditRoutineUiState())
    val uiState: StateFlow<EditRoutineUiState> = _uiState.asStateFlow()

    private var routineId: String? = null

    /** Sostiene a qué día vuelve el ejercicio elegido en el catálogo (M6, modo "elegir") — vive acá
     * y no en `remember` de la pantalla porque Navigation Compose descompone Editar rutina mientras
     * el catálogo está arriba en el backstack, así que el estado local de la pantalla no sobrevive
     * el viaje de ida y vuelta; el ViewModel sí, porque su ViewModelStore queda vivo. */
    private var pendingDayIdForPicker: String? = null

    init {
        if (initialRoutineId == NEW_ROUTINE_SENTINEL) {
            _uiState.update { it.copy(isLoading = false, needsName = true) }
        } else {
            routineId = initialRoutineId
            observeCurrentRoutine()
        }
    }

    private fun observeCurrentRoutine() {
        val id = routineId ?: return
        viewModelScope.launch {
            routineRepository.observeRoutine(id).collect { routine ->
                _uiState.update { it.copy(isLoading = false, needsName = false, routine = routine) }
            }
        }
    }

    fun createRoutine(name: String) {
        if (name.isBlank() || routineId != null) return
        viewModelScope.launch {
            routineId = routineRepository.createRoutine(name.trim())
            observeCurrentRoutine()
        }
    }

    fun addDay(name: String, dayOfWeek: DayOfWeek? = null) {
        val id = routineId ?: return
        if (name.isBlank()) return
        viewModelScope.launch { routineRepository.addDay(id, name.trim(), dayOfWeek) }
    }

    fun renameDay(dayId: String, newName: String) {
        val id = routineId ?: return
        if (newName.isBlank()) return
        viewModelScope.launch { routineRepository.renameDay(id, dayId, newName.trim()) }
    }

    fun removeDay(dayId: String) {
        val id = routineId ?: return
        viewModelScope.launch { routineRepository.removeDay(id, dayId) }
    }

    fun addExercise(dayId: String, exerciseId: String) {
        val id = routineId ?: return
        viewModelScope.launch { routineRepository.addExerciseToDay(id, dayId, exerciseId) }
    }

    fun beginPickingExerciseFor(dayId: String) {
        pendingDayIdForPicker = dayId
    }

    fun onExercisePicked(exerciseId: String) {
        val dayId = pendingDayIdForPicker ?: return
        pendingDayIdForPicker = null
        addExercise(dayId, exerciseId)
    }

    fun removeExercise(dayId: String, routineExerciseId: String) {
        val id = routineId ?: return
        viewModelScope.launch { routineRepository.removeExerciseFromDay(id, dayId, routineExerciseId) }
    }

    fun moveExercise(dayId: String, routineExerciseId: String, direction: MoveDirection) {
        val id = routineId ?: return
        viewModelScope.launch { routineRepository.moveExercise(id, dayId, routineExerciseId, direction) }
    }

    fun deleteRoutine() {
        val id = routineId ?: return
        viewModelScope.launch { routineRepository.deleteRoutine(id) }
    }
}
