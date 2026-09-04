package com.example.fitswap.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Fake para tests JVM: no simula el loop real (vive en [CardioTimerService], fuera del alcance
 * de un test unitario de ViewModel) — solo publica el estado. */
class FakeCardioTimer : CardioTimer {
    private val _state = MutableStateFlow(CardioTimerState(CardioTimerStatus.STOPPED, 0))
    override val state: StateFlow<CardioTimerState> = _state.asStateFlow()

    override fun start() {
        _state.value = CardioTimerState(CardioTimerStatus.RUNNING, 0)
    }

    override fun pause() {
        _state.update { it.copy(status = CardioTimerStatus.PAUSED) }
    }

    override fun resume() {
        _state.update { it.copy(status = CardioTimerStatus.RUNNING) }
    }

    override fun stop() {
        _state.update { it.copy(status = CardioTimerStatus.STOPPED) }
    }

    /** Solo para tests: simula el paso del tiempo sin loop real. */
    fun advanceTo(seconds: Int) {
        _state.update { it.copy(elapsedSeconds = seconds) }
    }
}
