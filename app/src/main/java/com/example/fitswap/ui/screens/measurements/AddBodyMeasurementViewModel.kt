package com.example.fitswap.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.data.repository.BodyProfileRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddBodyMeasurementUiState(
    val biologicalSex: BiologicalSex? = null,
    val heightCm: String = "",
    val weightKg: String = "",
    val neckCm: String = "",
    val waistCm: String = "",
    val hipCm: String = "",
    val chestCm: String = "",
    val armCm: String = "",
    val legCm: String = "",
    val calfCm: String = "",
) {
    val canSave: Boolean get() = weightKg.toDoubleOrNull() != null
}

@HiltViewModel
class AddBodyMeasurementViewModel @Inject constructor(
    private val measurementRepository: BodyMeasurementRepository,
    private val profileRepository: BodyProfileRepository,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBodyMeasurementUiState())
    val uiState: StateFlow<AddBodyMeasurementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.observeProfile().first()
            _uiState.update {
                it.copy(
                    biologicalSex = profile.biologicalSex,
                    heightCm = profile.heightCm?.let(::formatNumber).orEmpty(),
                )
            }
        }
    }

    fun onSexSelected(sex: BiologicalSex) {
        _uiState.update { it.copy(biologicalSex = sex) }
    }

    fun onHeightChanged(value: String) {
        _uiState.update { it.copy(heightCm = value) }
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

    /** Devuelve `false` sin persistir nada si falta el único campo obligatorio (peso). */
    suspend fun save(): Boolean {
        val state = _uiState.value
        val weight = state.weightKg.toDoubleOrNull() ?: return false

        profileRepository.updateProfile(
            BodyProfile(biologicalSex = state.biologicalSex, heightCm = state.heightCm.toDoubleOrNull())
        )
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
            )
        )
        return true
    }
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
