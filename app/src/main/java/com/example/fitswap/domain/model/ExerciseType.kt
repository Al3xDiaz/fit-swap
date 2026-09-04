package com.example.fitswap.domain.model

import kotlinx.serialization.Serializable

/** Distingue el flujo de registro durante el entrenamiento activo: [STRENGTH] usa series con
 * peso/reps (warmup/approach/effective, ver [SetType]); [CARDIO] es una sesión continua con
 * timer manual y datos de distancia/ritmo cardíaco/calorías (ver `CardioSession`). */
@Serializable
enum class ExerciseType { STRENGTH, CARDIO }
