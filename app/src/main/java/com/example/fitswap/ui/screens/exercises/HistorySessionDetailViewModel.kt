package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditableHistoryRow(
    val id: String,
    val type: SetType,
    val repsInput: String,
    val weightInput: String,
)

data class HistorySessionDetailUiState(
    val isLoading: Boolean = true,
    val exerciseName: String = "",
    val date: LocalDate = LocalDate.now(),
    val rows: List<EditableHistoryRow> = emptyList(),
    val noteText: String = "",
) {
    val canSave: Boolean get() = rows.all { it.repsInput.toIntOrNull() != null && it.weightInput.toDoubleOrNull() != null }
}

/**
 * Edita todas las series (efectivas y de aproximación) que un ejercicio registró en una fecha
 * puntual, más la nota de ese día — abierta desde el historial al tocar "editar" en un registro de
 * fuerza. Cada fila del historial de fuerza es un agregado por fecha sin id propio
 * ([com.example.fitswap.domain.logic.HistorySession]), así que esta pantalla resuelve
 * `(exerciseId, date)` a los [HistoryPoint] individuales de ese día.
 */
@HiltViewModel
class HistorySessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val historyRepository: HistoryRepository,
    private val notesRepository: NotesRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])
    private val date: LocalDate = LocalDate.parse(checkNotNull(savedStateHandle["date"]))

    private val _uiState = MutableStateFlow(HistorySessionDetailUiState(date = date))
    val uiState: StateFlow<HistorySessionDetailUiState> = _uiState.asStateFlow()

    /** Ids quitados localmente en esta edición (ver [removeRow]) — se borran recién al [save]. */
    private val removedIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExercise(exerciseId)
            _uiState.update { it.copy(exerciseName = exercise?.name.orEmpty()) }

            combine(
                historyRepository.observeHistoryForDate(exerciseId, date),
                notesRepository.observeNote(exerciseId, date),
            ) { points, note -> points to note }.first().let { (points, note) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rows = points.sortedBy { point -> point.id }.map { point -> point.toRow() },
                        noteText = note?.text.orEmpty(),
                    )
                }
            }
        }
    }

    fun onRepsChanged(rowId: String, value: String) {
        updateRow(rowId) { it.copy(repsInput = value) }
    }

    fun onWeightChanged(rowId: String, value: String) {
        updateRow(rowId) { it.copy(weightInput = value) }
    }

    fun onNoteTextChanged(value: String) {
        _uiState.update { it.copy(noteText = value) }
    }

    /** Quita una serie individual de la edición en curso — el borrado real ocurre en [save]. */
    fun removeRow(rowId: String) {
        removedIds += rowId
        _uiState.update { it.copy(rows = it.rows.filterNot { row -> row.id == rowId }) }
    }

    /** Devuelve `false` sin persistir nada si algún reps/peso quedó inválido. */
    suspend fun save(): Boolean {
        val state = _uiState.value
        if (!state.canSave) return false

        val points = state.rows.map { row ->
            HistoryPoint(
                id = row.id,
                exerciseId = exerciseId,
                date = date,
                type = row.type,
                reps = row.repsInput.toInt(),
                weightKg = row.weightInput.toDouble(),
            )
        }

        if (removedIds.isNotEmpty()) historyRepository.deleteHistoryPoints(removedIds.toList())
        points.forEach { historyRepository.updateHistoryPoint(it) }
        if (state.noteText.isNotBlank()) notesRepository.setNote(exerciseId, date, state.noteText)

        return true
    }

    private fun updateRow(rowId: String, transform: (EditableHistoryRow) -> EditableHistoryRow) {
        _uiState.update { state ->
            state.copy(rows = state.rows.map { row -> if (row.id == rowId) transform(row) else row })
        }
    }

    private fun HistoryPoint.toRow() = EditableHistoryRow(
        id = id,
        type = type,
        repsInput = "$reps",
        weightInput = "$weightKg",
    )
}
