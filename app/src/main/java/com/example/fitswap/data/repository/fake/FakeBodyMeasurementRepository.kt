package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.BodyMeasurementRepository
import com.example.fitswap.domain.model.BodyMeasurementEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeBodyMeasurementRepository @Inject constructor() : BodyMeasurementRepository {

    private val measurements = MutableStateFlow(seedMeasurements().sortedByDescending { it.date })

    override fun observeMeasurements(): Flow<List<BodyMeasurementEntry>> = measurements.asStateFlow()

    override suspend fun addMeasurement(entry: BodyMeasurementEntry) {
        measurements.update { current -> (current + entry).sortedByDescending { it.date } }
    }
}

/**
 * 3 mediciones de ejemplo espaciadas en el tiempo, solo con peso (sin circunferencias) — para que
 * el historial tenga con qué mostrarse desde el primer render, y a la vez ejercite la rama "faltan
 * datos para calcular % de grasa" por defecto (junto con el perfil vacío de
 * [FakeBodyProfileRepository]).
 */
private fun seedMeasurements(): List<BodyMeasurementEntry> = listOf(
    BodyMeasurementEntry(id = "seed-0", date = LocalDate.of(2026, 8, 1), weightKg = 78.0),
    BodyMeasurementEntry(id = "seed-1", date = LocalDate.of(2026, 8, 15), weightKg = 77.2),
    BodyMeasurementEntry(id = "seed-2", date = LocalDate.of(2026, 8, 29), weightKg = 76.5),
)
