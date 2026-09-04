package com.example.fitswap.data.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.ColorPalette
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val THEME = stringPreferencesKey("theme")
    val UI_DENSITY = stringPreferencesKey("ui_density")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    val REST_TIMER_SECONDS = intPreferencesKey("rest_timer_seconds")
    val COLOR_PALETTE = stringPreferencesKey("color_palette")
}

private val DEFAULT_SETTINGS = AppSettings()

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[Keys.THEME]?.let { AppTheme.valueOf(it) } ?: DEFAULT_SETTINGS.theme,
            uiDensity = prefs[Keys.UI_DENSITY]?.let { UiDensity.valueOf(it) } ?: DEFAULT_SETTINGS.uiDensity,
            unitSystem = prefs[Keys.UNIT_SYSTEM]?.let { UnitSystem.valueOf(it) } ?: DEFAULT_SETTINGS.unitSystem,
            restTimerSeconds = prefs[Keys.REST_TIMER_SECONDS] ?: DEFAULT_SETTINGS.restTimerSeconds,
            colorPalette = prefs[Keys.COLOR_PALETTE]?.let { ColorPalette.valueOf(it) } ?: DEFAULT_SETTINGS.colorPalette,
        )
    }

    override suspend fun updateTheme(theme: AppTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun updateUiDensity(uiDensity: UiDensity) {
        dataStore.edit { it[Keys.UI_DENSITY] = uiDensity.name }
    }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { it[Keys.UNIT_SYSTEM] = unitSystem.name }
    }

    override suspend fun updateRestTimerSeconds(restTimerSeconds: Int) {
        dataStore.edit { it[Keys.REST_TIMER_SECONDS] = restTimerSeconds }
    }

    override suspend fun updateColorPalette(colorPalette: ColorPalette) {
        dataStore.edit { it[Keys.COLOR_PALETTE] = colorPalette.name }
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME] = settings.theme.name
            prefs[Keys.UI_DENSITY] = settings.uiDensity.name
            prefs[Keys.UNIT_SYSTEM] = settings.unitSystem.name
            prefs[Keys.REST_TIMER_SECONDS] = settings.restTimerSeconds
            prefs[Keys.COLOR_PALETTE] = settings.colorPalette.name
        }
    }
}
