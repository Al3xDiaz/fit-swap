package com.example.fitswap.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.MeasurementField
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AddMeasurementStep { SELECT_FIELDS, FORM }

data class AddBodyMeasurementUiState(
    val step: AddMeasurementStep = AddMeasurementStep.SELECT_FIELDS,
    val selectedFields: Set<MeasurementField> = emptySet(),
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

/** Sexo/altura ya no se piden acá — son datos de perfil que se configuran una sola vez en
 * Configuración > Perfil (ver `SettingsViewModel`), no en cada medición. */
@HiltViewModel
class AddBodyMeasurementViewModel @Inject constructor(
    private val measurementRepository: BodyMeasurementRepository,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBodyMeasurementUiState())
    val uiState: StateFlow<AddBodyMeasurementUiState> = _uiState.asStateFlow()

    /** Alterna [field] en la selección del primer paso — al deseleccionarlo, limpia el texto que
     * hubiera quedado cargado en ese campo (si el usuario lo completó, volvió atrás y se arrepintió). */
    fun toggleField(field: MeasurementField) {
        _uiState.update { state ->
            val selected = state.selectedFields
            val newSelected = if (field in selected) selected - field else selected + field
            val cleared = if (field !in newSelected) state.withFieldCleared(field) else state
            cleared.copy(selectedFields = newSelected)
        }
    }

    fun goToForm() {
        _uiState.update { it.copy(step = AddMeasurementStep.FORM) }
    }

    fun goBackToFieldSelection() {
        _uiState.update { it.copy(step = AddMeasurementStep.SELECT_FIELDS) }
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

    /** Devuelve `false` sin persistir nada si falta el único campo obligatorio (peso). */
    suspend fun save(): Boolean {
        val state = _uiState.value
        val weight = state.weightKg.toDoubleOrNull() ?: return false

        measurementRepository.addMeasurement(
            BodyMeasurementEntry(
                id = "measurement-${System.nanoTime()}",
                date = currentDateProvider.today(),
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

private fun AddBodyMeasurementUiState.withFieldCleared(field: MeasurementField): AddBodyMeasurementUiState =
    when (field) {
        MeasurementField.NECK -> copy(neckCm = "")
        MeasurementField.WAIST -> copy(waistCm = "")
        MeasurementField.HIP -> copy(hipCm = "")
        MeasurementField.CHEST -> copy(chestCm = "")
        MeasurementField.ARM -> copy(armCm = "")
        MeasurementField.LEG -> copy(legCm = "")
        MeasurementField.CALF -> copy(calfCm = "")
        MeasurementField.GLUTE -> copy(gluteCm = "")
        MeasurementField.FOREARM -> copy(forearmCm = "")
        MeasurementField.SHOULDER -> copy(shoulderCm = "")
        MeasurementField.WRIST -> copy(wristCm = "")
    }
