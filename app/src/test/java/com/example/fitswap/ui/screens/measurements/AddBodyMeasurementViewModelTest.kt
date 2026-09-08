package com.example.fitswap.ui.screens.measurements

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeBodyMeasurementRepository
import com.example.fitswap.data.time.CurrentDateProvider
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

/** Sexo/altura ya no viven acá — se configuran una sola vez en Configuración > Perfil, cubierto
 * por `SettingsViewModelTest`. */
class AddBodyMeasurementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        measurementRepository: FakeBodyMeasurementRepository = FakeBodyMeasurementRepository(),
    ) = AddBodyMeasurementViewModel(measurementRepository, fixedToday)

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
    fun `guardar con peso agrega la medicion`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val viewModel = viewModel(measurementRepository)

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
    }

    @Test
    fun `guardar persiste gluteo antebrazo hombro y muneca`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val viewModel = viewModel(measurementRepository)

        viewModel.onWeightChanged("80.5")
        viewModel.onGluteChanged("98")
        viewModel.onForearmChanged("27")
        viewModel.onShoulderChanged("112")
        viewModel.onWristChanged("16.5")

        val saved = viewModel.save()

        assertTrue(saved)
        val entry = measurementRepository.observeMeasurements().first().first()
        assertEquals(98.0, entry.gluteCm)
        assertEquals(27.0, entry.forearmCm)
        assertEquals(112.0, entry.shoulderCm)
        assertEquals(16.5, entry.wristCm)
    }
}
