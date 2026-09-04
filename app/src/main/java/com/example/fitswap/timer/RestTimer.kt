package com.example.fitswap.timer

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato observado por `ActiveExerciseViewModel` para el descanso entre series — separado de
 * [AndroidRestTimerController] (la implementación real, respaldada por un foreground service) para
 * poder inyectar un fake en tests JVM, igual que el resto de repositorios de la app.
 */
interface RestTimer {
    val remainingSeconds: StateFlow<Int?>
    fun start(totalSeconds: Int)
}
