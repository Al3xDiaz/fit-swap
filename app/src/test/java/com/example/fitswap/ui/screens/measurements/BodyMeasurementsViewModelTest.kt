package com.example.fitswap.ui.screens.measurements

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeBodyMeasurementRepository
import com.example.fitswap.data.repository.fake.FakeBodyProfileRepository
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BodyMeasurementsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `sin perfil completo el imc y el porcentaje de grasa vienen nulos`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val profileRepository = FakeBodyProfileRepository()
        val viewModel = BodyMeasurementsViewModel(measurementRepository, profileRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(3, state.rows.size)
        state.rows.forEach { row ->
            assertNull(row.bmi)
            assertNull(row.bodyFatPercent)
        }
    }

    @Test
    fun `con perfil completo y medicion con circunferencias se calculan imc y porcentaje de grasa`() = runTest {
        val measurementRepository = FakeBodyMeasurementRepository()
        val profileRepository = FakeBodyProfileRepository()
        profileRepository.updateProfile(BodyProfile(biologicalSex = BiologicalSex.MALE, heightCm = 180.0))
        measurementRepository.addMeasurement(
            BodyMeasurementEntry(
                id = "full", date = LocalDate.of(2026, 9, 2), weightKg = 81.0, neckCm = 38.0, waistCm = 85.0,
            )
        )
        val viewModel = BodyMeasurementsViewModel(measurementRepository, profileRepository)

        val state = viewModel.uiState.first { !it.isLoading }
        val row = state.rows.first { it.entry.id == "full" }

        assertNotNull(row.bmi)
        assertNotNull(row.bmiCategory)
        assertNotNull(row.bodyFatPercent)
    }
}
