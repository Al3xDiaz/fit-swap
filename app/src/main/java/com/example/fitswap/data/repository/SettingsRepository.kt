package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateTheme(theme: AppTheme)
    suspend fun updateUiDensity(uiDensity: UiDensity)
    suspend fun updateUnitSystem(unitSystem: UnitSystem)
    suspend fun updateRestTimerSeconds(restTimerSeconds: Int)

    /** Usado por Herramientas (M9) al importar un backup. */
    suspend fun replaceSettings(settings: AppSettings)
}
