package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.BodyMeasurementEntry
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeBodyMeasurementRepositoryTest {

    @Test
    fun `addMeasurement con un id ya existente reemplaza en vez de duplicar`() = runTest {
        val repository = FakeBodyMeasurementRepository()
        repository.addMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 80.0))

        repository.addMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 81.5))

        val measurements = repository.observeMeasurements().first()
        assertEquals(1, measurements.count { it.id == "m1" })
        assertEquals(81.5, measurements.single { it.id == "m1" }.weightKg, 0.0)
    }

    @Test
    fun `updateMeasurement reemplaza los datos de una medicion existente por su id`() = runTest {
        val repository = FakeBodyMeasurementRepository()
        repository.addMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 80.0))

        repository.updateMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 82.0, neckCm = 39.0))

        val measurements = repository.observeMeasurements().first()
        assertEquals(1, measurements.count { it.id == "m1" })
        val updated = measurements.single { it.id == "m1" }
        assertEquals(82.0, updated.weightKg, 0.0)
        assertEquals(39.0, updated.neckCm)
    }

    @Test
    fun `updateMeasurement no afecta otras mediciones`() = runTest {
        val repository = FakeBodyMeasurementRepository()
        repository.addMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 80.0))
        repository.addMeasurement(BodyMeasurementEntry(id = "m2", date = LocalDate.of(2026, 9, 2), weightKg = 79.0))

        repository.updateMeasurement(BodyMeasurementEntry(id = "m1", date = LocalDate.of(2026, 9, 1), weightKg = 82.0))

        val measurements = repository.observeMeasurements().first()
        assertEquals(79.0, measurements.single { it.id == "m2" }.weightKg, 0.0)
    }
}
