package com.example.fitswap.ui.screens.tools

import androidx.lifecycle.ViewModel
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.ExportedData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

data class ToolsUiState(val message: String? = null)

private val exportImportJson = Json { prettyPrint = true }

/**
 * El I/O de archivos (SAF) vive en `ToolsScreen`, no acá — este ViewModel es JVM puro para poder
 * testear el round-trip export→import sin depender de `Context`/`ContentResolver`.
 */
@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val historyRepository: HistoryRepository,
    private val notesRepository: NotesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    suspend fun buildExportJson(): String {
        val data = ExportedData(
            routines = routineRepository.observeRoutines().first(),
            history = historyRepository.observeAllHistory().first(),
            notes = notesRepository.observeAllNotes().first(),
            settings = settingsRepository.observeSettings().first(),
        )
        return exportImportJson.encodeToString(ExportedData.serializer(), data)
    }

    fun onExportSucceeded() {
        _uiState.update { it.copy(message = "Exportación completa") }
    }

    fun onExportFailed(reason: String) {
        _uiState.update { it.copy(message = "Error al exportar: $reason") }
    }

    fun onImportFailed(reason: String) {
        _uiState.update { it.copy(message = "Error al importar: $reason") }
    }

    suspend fun importFromJson(jsonText: String) {
        val data = try {
            exportImportJson.decodeFromString(ExportedData.serializer(), jsonText)
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "Error al importar: ${e.message ?: "archivo inválido"}") }
            return
        }
        routineRepository.replaceRoutines(data.routines)
        historyRepository.replaceAllHistory(data.history)
        notesRepository.replaceAllNotes(data.notes)
        settingsRepository.replaceSettings(data.settings)
        _uiState.update { it.copy(message = "Importación completa") }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
