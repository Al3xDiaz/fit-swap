package com.example.fitswap.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.BodyProfileRepository
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyProfile
import com.example.fitswap.domain.model.ColorPalette
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bodyProfileRepository: BodyProfileRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val profile: StateFlow<BodyProfile> = bodyProfileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyProfile())

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.updateTheme(theme) }
    }

    fun onUiDensitySelected(uiDensity: UiDensity) {
        viewModelScope.launch { settingsRepository.updateUiDensity(uiDensity) }
    }

    fun onUnitSystemSelected(unitSystem: UnitSystem) {
        viewModelScope.launch { settingsRepository.updateUnitSystem(unitSystem) }
    }

    fun onRestTimerSecondsSelected(restTimerSeconds: Int) {
        viewModelScope.launch { settingsRepository.updateRestTimerSeconds(restTimerSeconds) }
    }

    fun onColorPaletteSelected(colorPalette: ColorPalette) {
        viewModelScope.launch { settingsRepository.updateColorPalette(colorPalette) }
    }

    fun onSexSelected(sex: BiologicalSex) {
        viewModelScope.launch {
            val current = bodyProfileRepository.observeProfile().first()
            bodyProfileRepository.updateProfile(current.copy(biologicalSex = sex))
        }
    }

    fun onHeightChanged(heightCm: Double) {
        viewModelScope.launch {
            val current = bodyProfileRepository.observeProfile().first()
            bodyProfileRepository.updateProfile(current.copy(heightCm = heightCm))
        }
    }
}
