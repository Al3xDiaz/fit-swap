package com.example.fitswap.domain.model

import java.time.LocalDate
import kotlinx.serialization.Serializable

/**
 * Un registro de medición corporal. Solo [weightKg] es obligatorio — las circunferencias son
 * opcionales (el usuario puede registrar solo peso); cuello/cintura/cadera además alimentan
 * `BodyMetrics.bodyFatPercent` (fórmula US Navy) cuando están completas.
 */
@Serializable
data class BodyMeasurementEntry(
    val id: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val weightKg: Double,
    val neckCm: Double? = null,
    val waistCm: Double? = null,
    val hipCm: Double? = null,
    val chestCm: Double? = null,
    val armCm: Double? = null,
    val legCm: Double? = null,
    val calfCm: Double? = null,
)
