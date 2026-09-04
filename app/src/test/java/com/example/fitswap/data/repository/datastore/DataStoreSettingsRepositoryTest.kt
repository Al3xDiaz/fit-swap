package com.example.fitswap.data.repository.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.fitswap.domain.model.AppSettings
import com.example.fitswap.domain.model.AppTheme
import com.example.fitswap.domain.model.ColorPalette
import com.example.fitswap.domain.model.UiDensity
import com.example.fitswap.domain.model.UnitSystem
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreSettingsRepositoryTest {

    private fun newRepository(): DataStoreSettingsRepository {
        val file = File.createTempFile("test_settings", ".preferences_pb")
        file.deleteOnExit()
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        return DataStoreSettingsRepository(dataStore)
    }

    @Test
    fun `sin valores guardados expone los defaults`() = runTest {
        val repository = newRepository()

        val settings = repository.observeSettings().first()

        assertEquals(AppSettings(), settings)
    }

    @Test
    fun `actualizar cada campo se refleja en observeSettings`() = runTest {
        val repository = newRepository()

        repository.updateTheme(AppTheme.DARK)
        repository.updateUiDensity(UiDensity.COMPACT)
        repository.updateUnitSystem(UnitSystem.IMPERIAL)
        repository.updateRestTimerSeconds(45)
        repository.updateColorPalette(ColorPalette.STEEL)

        val settings = repository.observeSettings().first()
        assertEquals(AppTheme.DARK, settings.theme)
        assertEquals(UiDensity.COMPACT, settings.uiDensity)
        assertEquals(UnitSystem.IMPERIAL, settings.unitSystem)
        assertEquals(45, settings.restTimerSeconds)
        assertEquals(ColorPalette.STEEL, settings.colorPalette)
    }

    @Test
    fun `replaceSettings sobreescribe todos los campos a la vez`() = runTest {
        val repository = newRepository()
        repository.updateTheme(AppTheme.DARK)

        repository.replaceSettings(AppSettings(theme = AppTheme.LIGHT, restTimerSeconds = 60))

        val settings = repository.observeSettings().first()
        assertEquals(AppTheme.LIGHT, settings.theme)
        assertEquals(60, settings.restTimerSeconds)
    }
}
