package com.example.fitswap.domain.model

import java.time.LocalDate

/** Una sesión continua de cardio (sin series como en [SetType]/[LoggedSet]): el usuario inicia un
 * timer, hace el cardio, lo detiene, y registra los datos de esa sesión completa. Distancia,
 * ritmo cardíaco y calorías son de entrada manual (sin sensores/Health Connect) — nulos si el
 * usuario no los completó. */
data class CardioSession(
    val id: String,
    val exerciseId: String,
    val date: LocalDate,
    val durationSeconds: Int,
    val distanceKm: Double?,
    val avgHeartRate: Int?,
    val calories: Int?,
)
