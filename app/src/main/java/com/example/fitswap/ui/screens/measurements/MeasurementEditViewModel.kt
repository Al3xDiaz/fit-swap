package com.example.fitswap.ui.screens.measurements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.domain.model.BodyMeasurementEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementEditUiState(
    val isLoading: Boolean = true,
    val weightKg: String = "",
    val neckCm: String = "",
    val waistCm: String = "",
    val hipCm: String = "",
    val chestCm: String = "",
    val armCm: String = "",
    val legCm: String = "",
    val calfCm: String = "",
    val gluteCm: String = "",
    val forearmCm: String = "",
    val shoulderCm: String = "",
    val wristCm: String = "",
) {
    val canSave: Boolean get() = weightKg.toDoubleOrNull() != null
}

/** Edita una [BodyMeasurementEntry] ya guardada — a diferencia del alta (stepper de 2 pasos), es
 * una sola página con los 12 campos, prefilled con los valores existentes (vacíos si nunca se
 * cargaron). La fecha no es editable — se conserva la de la medición original. */
@HiltViewModel
class MeasurementEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val measurementRepository: BodyMeasurementRepository,
) : ViewModel() {

    private val measurementId: String = checkNotNull(savedStateHandle["measurementId"])
    private var originalDate: LocalDate = LocalDate.now()

    private val _uiState = MutableStateFlow(MeasurementEditUiState())
    val uiState: StateFlow<MeasurementEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val entry = measurementRepository.observeMeasurements().first().find { it.id == measurementId }
            if (entry != null) originalDate = entry.date

            _uiState.update {
                it.copy(
                    isLoading = false,
                    weightKg = entry?.weightKg?.let(::formatNumber).orEmpty(),
                    neckCm = entry?.neckCm?.let(::formatNumber).orEmpty(),
                    waistCm = entry?.waistCm?.let(::formatNumber).orEmpty(),
                    hipCm = entry?.hipCm?.let(::formatNumber).orEmpty(),
                    chestCm = entry?.chestCm?.let(::formatNumber).orEmpty(),
                    armCm = entry?.armCm?.let(::formatNumber).orEmpty(),
                    legCm = entry?.legCm?.let(::formatNumber).orEmpty(),
                    calfCm = entry?.calfCm?.let(::formatNumber).orEmpty(),
                    gluteCm = entry?.gluteCm?.let(::formatNumber).orEmpty(),
                    forearmCm = entry?.forearmCm?.let(::formatNumber).orEmpty(),
                    shoulderCm = entry?.shoulderCm?.let(::formatNumber).orEmpty(),
                    wristCm = entry?.wristCm?.let(::formatNumber).orEmpty(),
                )
            }
        }
    }

    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weightKg = value) }
    }

    fun onNeckChanged(value: String) {
        _uiState.update { it.copy(neckCm = value) }
    }

    fun onWaistChanged(value: String) {
        _uiState.update { it.copy(waistCm = value) }
    }

    fun onHipChanged(value: String) {
        _uiState.update { it.copy(hipCm = value) }
    }

    fun onChestChanged(value: String) {
        _uiState.update { it.copy(chestCm = value) }
    }

    fun onArmChanged(value: String) {
        _uiState.update { it.copy(armCm = value) }
    }

    fun onLegChanged(value: String) {
        _uiState.update { it.copy(legCm = value) }
    }

    fun onCalfChanged(value: String) {
        _uiState.update { it.copy(calfCm = value) }
    }

    fun onGluteChanged(value: String) {
        _uiState.update { it.copy(gluteCm = value) }
    }

    fun onForearmChanged(value: String) {
        _uiState.update { it.copy(forearmCm = value) }
    }

    fun onShoulderChanged(value: String) {
        _uiState.update { it.copy(shoulderCm = value) }
    }

    fun onWristChanged(value: String) {
        _uiState.update { it.copy(wristCm = value) }
    }

    /** Devuelve `false` sin persistir nada si falta el único campo obligatorio (peso). Conserva el
     * mismo id y la fecha original de la medición. */
    suspend fun save(): Boolean {
        val state = _uiState.value
        val weight = state.weightKg.toDoubleOrNull() ?: return false

        measurementRepository.updateMeasurement(
            BodyMeasurementEntry(
                id = measurementId,
                date = originalDate,
                weightKg = weight,
                neckCm = state.neckCm.toDoubleOrNull(),
                waistCm = state.waistCm.toDoubleOrNull(),
                hipCm = state.hipCm.toDoubleOrNull(),
                chestCm = state.chestCm.toDoubleOrNull(),
                armCm = state.armCm.toDoubleOrNull(),
                legCm = state.legCm.toDoubleOrNull(),
                calfCm = state.calfCm.toDoubleOrNull(),
                gluteCm = state.gluteCm.toDoubleOrNull(),
                forearmCm = state.forearmCm.toDoubleOrNull(),
                shoulderCm = state.shoulderCm.toDoubleOrNull(),
                wristCm = state.wristCm.toDoubleOrNull(),
            )
        )
        return true
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.1f".format(value)
