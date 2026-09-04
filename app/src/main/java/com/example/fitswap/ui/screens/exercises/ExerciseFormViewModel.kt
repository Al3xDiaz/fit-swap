package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.GalleryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val VIDEO_URL_EXTENSIONS = listOf(".mp4", ".mov", ".webm", ".mkv", ".avi")

const val NEW_EXERCISE_ID = "new"

data class ExerciseFormUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val name: String = "",
    val muscleGroup: String = "",
    val equipment: String = "",
    val tags: List<String> = emptyList(),
    val newTagText: String = "",
) {
    val canSave: Boolean get() = name.isNotBlank() && muscleGroup.isNotBlank() && equipment.isNotBlank()
}

@HiltViewModel
class ExerciseFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val galleryRepository: GalleryRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])
    private val isNewExercise: Boolean = exerciseId == NEW_EXERCISE_ID

    private val _uiState = MutableStateFlow(ExerciseFormUiState(isNew = isNewExercise))
    val uiState: StateFlow<ExerciseFormUiState> = _uiState.asStateFlow()

    /** Solo tiene sentido para un ejercicio ya guardado — mientras se crea uno nuevo ("new") no
     * hay id persistido todavía al que atar fotos/videos. */
    val galleryItems: StateFlow<List<GalleryItem>> =
        (if (isNewExercise) flowOf(emptyList()) else galleryRepository.observeGallery(exerciseId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (exerciseId == NEW_EXERCISE_ID) {
            _uiState.update { it.copy(isLoading = false) }
        } else {
            viewModelScope.launch {
                val exercise = exerciseRepository.getExercise(exerciseId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = exercise?.name.orEmpty(),
                        muscleGroup = exercise?.muscleGroup.orEmpty(),
                        equipment = exercise?.equipment.orEmpty(),
                        tags = exercise?.tags.orEmpty(),
                    )
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onMuscleGroupChanged(value: String) {
        _uiState.update { it.copy(muscleGroup = value) }
    }

    fun onEquipmentChanged(value: String) {
        _uiState.update { it.copy(equipment = value) }
    }

    fun onNewTagTextChanged(value: String) {
        _uiState.update { it.copy(newTagText = value) }
    }

    fun addTag() {
        val tag = _uiState.value.newTagText.trim()
        if (tag.isEmpty()) return
        _uiState.update {
            if (it.tags.contains(tag)) it.copy(newTagText = "") else it.copy(tags = it.tags + tag, newTagText = "")
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    suspend fun save(): Boolean {
        val state = _uiState.value
        if (!state.canSave) return false
        val exercise = Exercise(
            id = if (state.isNew) "exercise-${System.nanoTime()}" else exerciseId,
            name = state.name.trim(),
            muscleGroup = state.muscleGroup.trim(),
            equipment = state.equipment.trim(),
            tags = state.tags,
        )
        if (state.isNew) exerciseRepository.addExercise(exercise) else exerciseRepository.updateExercise(exercise)
        return true
    }

    /** `false` si el ejercicio sigue en uso en alguna rutina (no se borra en ese caso). */
    suspend fun deleteExercise(): Boolean {
        if (_uiState.value.isNew) return false
        return exerciseRepository.deleteExercise(exerciseId)
    }

    fun addMedia(uri: String, isVideo: Boolean) {
        if (isNewExercise) return
        viewModelScope.launch { galleryRepository.addMedia(exerciseId, uri, isVideo) }
    }

    /** Agrega multimedia por enlace web en vez del picker del sistema — el tipo se infiere de
     * la extensión del enlace (heurística simple, no hay `ContentResolver` para un http URL). */
    fun addMediaFromUrl(url: String) {
        if (isNewExercise) return
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val isVideo = VIDEO_URL_EXTENSIONS.any { trimmed.substringBefore('?').endsWith(it, ignoreCase = true) }
        viewModelScope.launch { galleryRepository.addMedia(exerciseId, trimmed, isVideo) }
    }

    fun deleteMedia(id: String) {
        viewModelScope.launch { galleryRepository.deleteMedia(id) }
    }
}
