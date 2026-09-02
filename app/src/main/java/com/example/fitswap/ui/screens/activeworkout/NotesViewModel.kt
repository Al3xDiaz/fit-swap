package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.domain.model.Note
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
    val notes: List<Note> = emptyList(),
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val notesRepository: NotesRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
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

            notesRepository.observeNotes(exercise.id).collect { notes ->
                _uiState.update { it.copy(isLoading = false, exerciseName = exercise.name, notes = notes) }
            }
        }
    }

    fun addNote(text: String) {
        val id = exerciseId ?: return
        if (text.isBlank()) return
        viewModelScope.launch { notesRepository.addNote(id, text) }
    }
}
