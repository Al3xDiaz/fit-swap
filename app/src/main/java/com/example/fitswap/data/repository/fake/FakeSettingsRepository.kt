package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeSettingsRepository @Inject constructor() : SettingsRepository {

    private val settings = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = settings.asStateFlow()

    override suspend fun updateTheme(theme: AppTheme) {
        settings.update { it.copy(theme = theme) }
    }

    override suspend fun updateUiDensity(uiDensity: UiDensity) {
        settings.update { it.copy(uiDensity = uiDensity) }
    }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        settings.update { it.copy(unitSystem = unitSystem) }
    }

    override suspend fun updateRestTimerSeconds(restTimerSeconds: Int) {
        settings.update { it.copy(restTimerSeconds = restTimerSeconds) }
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        this.settings.value = settings
    }
}
