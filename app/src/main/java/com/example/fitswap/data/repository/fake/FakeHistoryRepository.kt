package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Solo "Press de pecho" y "Sentadilla / hack squat / prensa" tienen historial previo: sirve para
 * ejercitar ambas ramas de la regla de peso sugerido (último registrado vs. 1 kg por defecto) al
 * navegar por distintos ejercicios de la rutina por defecto.
 */
@Singleton
class FakeHistoryRepository @Inject constructor() : HistoryRepository {

    private val lastWeightsByExerciseId = mapOf(
        "press-de-pecho" to 60.0,
        "sentadilla-hack-prensa" to 80.0,
    )

    override suspend fun lastWeightKg(exerciseId: String): Double? = lastWeightsByExerciseId[exerciseId]
}
