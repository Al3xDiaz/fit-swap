package com.example.fitswap.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.data.repository.BodyProfileRepository
import com.example.fitswap.domain.logic.BmiCategory
import com.example.fitswap.domain.logic.BodyMetrics
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BodyMeasurementRow(
    val entry: BodyMeasurementEntry,
    val bmi: Double?,
    val bmiCategory: BmiCategory?,
    val bodyFatPercent: Double?,
)

data class BodyMeasurementsUiState(
    val isLoading: Boolean = true,
    val rows: List<BodyMeasurementRow> = emptyList(),
    val profile: BodyProfile = BodyProfile(),
)

@HiltViewModel
class BodyMeasurementsViewModel @Inject constructor(
    private val measurementRepository: BodyMeasurementRepository,
    profileRepository: BodyProfileRepository,
) : ViewModel() {

    fun deleteMeasurement(id: String) {
        viewModelScope.launch { measurementRepository.deleteMeasurement(id) }
    }

    val uiState: StateFlow<BodyMeasurementsUiState> = combine(
        measurementRepository.observeMeasurements(),
        profileRepository.observeProfile(),
    ) { measurements, profile ->
        BodyMeasurementsUiState(
            isLoading = false,
            rows = measurements.map { entry -> entry.toRow(profile) },
            profile = profile,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyMeasurementsUiState())

    private fun BodyMeasurementEntry.toRow(profile: BodyProfile): BodyMeasurementRow {
        val bmi = profile.heightCm?.let { height -> BodyMetrics.bmi(weightKg, height) }
        return BodyMeasurementRow(
            entry = this,
            bmi = bmi,
            bmiCategory = bmi?.let { BodyMetrics.bmiCategory(it) },
            bodyFatPercent = BodyMetrics.bodyFatPercent(profile, this),
        )
    }
}
