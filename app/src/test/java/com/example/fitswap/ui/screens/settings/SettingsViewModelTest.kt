package com.example.fitswap.ui.screens.settings

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeBodyProfileRepository
import com.example.fitswap.data.repository.fake.FakeSettingsRepository
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.ColorPalette
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        profileRepository: FakeBodyProfileRepository = FakeBodyProfileRepository(),
    ) = SettingsViewModel(settingsRepository, profileRepository)

    @Test
    fun `elegir sexo actualiza el perfil sin tocar la altura ya guardada`() = runTest {
        val profileRepository = FakeBodyProfileRepository()
        val viewModel = viewModel(profileRepository = profileRepository)
        viewModel.onHeightChanged(180.0)

        viewModel.onSexSelected(BiologicalSex.MALE)

        val profile = viewModel.profile.first { it.biologicalSex != null }
        assertEquals(BiologicalSex.MALE, profile.biologicalSex)
        assertEquals(180.0, profile.heightCm)
    }

    @Test
    fun `elegir paleta de colores se refleja en settings`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settingsRepository)

        viewModel.onColorPaletteSelected(ColorPalette.NEON)

        assertEquals(ColorPalette.NEON, viewModel.settings.first { it.colorPalette == ColorPalette.NEON }.colorPalette)
    }
}
