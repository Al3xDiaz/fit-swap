package com.example.fitswap.ui.screens.measurements

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeBodyMeasurementRepository
import com.example.fitswap.data.repository.fake.FakeBodyProfileRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val fixedToday = object : CurrentDateProvider {
    override fun today(): LocalDate = LocalDate.of(2026, 9, 2)
}

class AddBodyMeasurementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        measurementRepository: FakeBodyMeasurementRepository = FakeBodyMeasurementRepository(),
        profileRepository: FakeBodyProfileRepository = FakeBodyProfileRepository(),
    ) = AddBodyMeasurementViewModel(measurementRepository, profileRepository, fixedToday)

    @Test
    fun `precarga sexo y altura desde el perfil existente`() = runTest {
        val profileRepository = FakeBodyProfileRepository()
        profileRepository.updateProfile(BodyProfile(biologicalSex = BiologicalSex.FEMALE, heightCm = 165.0))

        val viewModel = viewModel(profileRepository = profileRepository)
        val state = viewModel.uiState.first { it.biologicalSex != null }

        assertEquals(BiologicalSex.FEMALE, state.biologicalSex)
        assertEquals("165", state.heightCm)
    }

    @Test
    fun `sin peso cargado no se puede guardar`() = runTest {
        val viewModel = viewModel()

        assertFalse(viewModel.uiState.first().canSave)
    }

    @Test
    fun `guardar sin peso no persiste nada y devuelve false`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val viewModel = viewModel(measurementRepository = measurementRepository)
        val before = measurementRepository.observeMeasurements().first()

        val saved = viewModel.save()

        assertFalse(saved)
        assertEquals(before, measurementRepository.observeMeasurements().first())
    }

    @Test
    fun `guardar con peso agrega la medicion y actualiza el perfil`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val profileRepository = FakeBodyProfileRepository()
        val viewModel = viewModel(measurementRepository, profileRepository)

        viewModel.onSexSelected(BiologicalSex.MALE)
        viewModel.onHeightChanged("180")
        viewModel.onWeightChanged("80.5")
        viewModel.onNeckChanged("38")
        viewModel.onWaistChanged("85")

        val saved = viewModel.save()

        assertTrue(saved)
        val measurements = measurementRepository.observeMeasurements().first()
        val entry = measurements.first()
        assertEquals(80.5, entry.weightKg, 0.0)
        assertEquals(38.0, entry.neckCm)
        assertEquals(85.0, entry.waistCm)
        assertEquals(LocalDate.of(2026, 9, 2), entry.date)
        assertEquals(BodyProfile(biologicalSex = BiologicalSex.MALE, heightCm = 180.0), profileRepository.observeProfile().first())
    }
}
