package com.example.fitswap.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

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
}
