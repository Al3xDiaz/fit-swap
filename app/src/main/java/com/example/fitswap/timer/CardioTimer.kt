package com.example.fitswap.timer

import kotlinx.coroutines.flow.StateFlow

enum class CardioTimerStatus { STOPPED, RUNNING, PAUSED }

data class CardioTimerState(val status: CardioTimerStatus, val elapsedSeconds: Int)

/**
 * Contrato observado por `ActiveExerciseViewModel` para el timer de cardio — a diferencia de
 * [RestTimer] (cuenta regresiva con total conocido, se auto-inicia), este cuenta hacia arriba
 * desde 0 y requiere control manual (iniciar/pausar/reanudar/detener), ver [CardioTimerService].
 */
interface CardioTimer {
    val state: StateFlow<CardioTimerState>
    fun start()
    fun pause()
    fun resume()
    fun stop()
}
