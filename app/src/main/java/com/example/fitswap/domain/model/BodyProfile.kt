package com.example.fitswap.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BiologicalSex { MALE, FEMALE }

/**
 * Datos que cambian rara vez (a diferencia de una medición, que se registra periódicamente) — ver
 * `BodyMeasurementEntry`. Ambos campos nulos hasta que el usuario complete su perfil por primera
 * vez; requeridos por [com.example.fitswap.domain.logic.BodyMetrics.bodyFatPercent].
 */
@Serializable
data class BodyProfile(
    val biologicalSex: BiologicalSex? = null,
    val heightCm: Double? = null,
)
