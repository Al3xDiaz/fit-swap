package com.example.fitswap.ui.screens.measurements

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeBodyMeasurementRepository
import com.example.fitswap.domain.model.BodyMeasurementEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val MEASUREMENT_ID = "measurement-1"

class MeasurementEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(measurementRepository: FakeBodyMeasurementRepository) = MeasurementEditViewModel(
        savedStateHandle = SavedStateHandle(mapOf("measurementId" to MEASUREMENT_ID)),
        measurementRepository = measurementRepository,
    )

    @Test
    fun `precarga los campos con los datos de la medicion existente`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        measurementRepository.addMeasurement(
            BodyMeasurementEntry(
                id = MEASUREMENT_ID, date = LocalDate.of(2026, 9, 1), weightKg = 80.0,
                neckCm = 38.0, waistCm = 85.0,
            )
        )
        val viewModel = viewModel(measurementRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("80", state.weightKg)
        assertEquals("38", state.neckCm)
        assertEquals("85", state.waistCm)
        assertEquals("", state.hipCm) // nunca se cargó, queda vacío en vez de inventado
    }

    @Test
    fun `guardar conserva el id y la fecha originales, con los campos editados`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        measurementRepository.addMeasurement(
            BodyMeasurementEntry(id = MEASUREMENT_ID, date = LocalDate.of(2026, 9, 1), weightKg = 80.0, neckCm = 38.0)
        )
        val viewModel = viewModel(measurementRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onWeightChanged("81.5")
        viewModel.onGluteChanged("98")
        val saved = viewModel.save()

        assertTrue(saved)
        val entry = measurementRepository.observeMeasurements().first().single { it.id == MEASUREMENT_ID }
        assertEquals(MEASUREMENT_ID, entry.id)
        assertEquals(LocalDate.of(2026, 9, 1), entry.date)
        assertEquals(81.5, entry.weightKg, 0.0)
        assertEquals(38.0, entry.neckCm)
        assertEquals(98.0, entry.gluteCm)
    }

    @Test
    fun `guardar no duplica la medicion, la reemplaza por id`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        measurementRepository.addMeasurement(
            BodyMeasurementEntry(id = MEASUREMENT_ID, date = LocalDate.of(2026, 9, 1), weightKg = 80.0)
        )
        val viewModel = viewModel(measurementRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onWeightChanged("81.5")
        viewModel.save()

        // El fake siembra 3 mediciones de ejemplo (seed-0/1/2) además de la de este test.
        assertEquals(1, measurementRepository.observeMeasurements().first().count { it.id == MEASUREMENT_ID })
    }

    @Test
    fun `guardar sin peso no persiste nada`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        measurementRepository.addMeasurement(
            BodyMeasurementEntry(id = MEASUREMENT_ID, date = LocalDate.of(2026, 9, 1), weightKg = 80.0)
        )
        val viewModel = viewModel(measurementRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onWeightChanged("")
        val saved = viewModel.save()

        assertFalse(saved)
        assertEquals(80.0, measurementRepository.observeMeasurements().first().single { it.id == MEASUREMENT_ID }.weightKg, 0.0)
    }

    @Test
    fun `una medicion inexistente no rompe la carga, queda con los campos vacios`() = runTest {
        val viewModel = viewModel(FakeBodyMeasurementRepository())

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals("", state.weightKg)
        assertNull(state.weightKg.toDoubleOrNull())
    }
}
