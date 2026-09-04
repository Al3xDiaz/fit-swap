package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.time.CurrentDateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    /** Nota del día de hoy para este ejercicio — una sola, no una lista (ver [Note]). */
    val noteText: String = "",
)

/** Edita la nota de **hoy** para el ejercicio activo — escribirla de nuevo el mismo día
 * reemplaza lo anterior, no se acumula una lista (ver `NotesRepository.setNote`). Esa nota es la
 * que después se muestra junto a la sesión de hoy en el historial del ejercicio. */
@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val notesRepository: NotesRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])
    private val dayId: String = checkNotNull(savedStateHandle["dayId"])
    private val routineExerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var exerciseId: String? = null

    init {
        viewModelScope.launch {
            val routine = routineRepository.observeRoutine(routineId).first { it != null } ?: return@launch
            val day = routine.days.firstOrNull { it.id == dayId } ?: return@launch
            val routineExercise = day.exercises.firstOrNull { it.id == routineExerciseId } ?: return@launch

            val substitution = workoutSessionRepository.observeSubstitution(routineExerciseId).first()
            val exercise = substitution ?: routineExercise.exercise
            exerciseId = exercise.id

            notesRepository.observeNote(exercise.id, currentDateProvider.today()).collect { note ->
                _uiState.update { it.copy(isLoading = false, exerciseName = exercise.name, noteText = note?.text.orEmpty()) }
            }
        }
    }

    fun onNoteTextChanged(text: String) {
        _uiState.update { it.copy(noteText = text) }
    }

    fun save() {
        val id = exerciseId ?: return
        val text = _uiState.value.noteText
        if (text.isBlank()) return
        viewModelScope.launch { notesRepository.setNote(id, currentDateProvider.today(), text) }
    }
}
