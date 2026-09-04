package com.example.fitswap.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.ColorPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ThemeViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = settingsRepository.observeSettings()
        .map { it.theme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.SYSTEM)

    val colorPalette: StateFlow<ColorPalette> = settingsRepository.observeSettings()
        .map { it.colorPalette }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ColorPalette.DYNAMIC)
}
