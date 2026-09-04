package com.example.fitswap.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fake para tests JVM: [start] solo publica el total inicial, no simula el conteo en tiempo real
 * (el conteo real vive en [RestTimerService], fuera del alcance de un test unitario de ViewModel). */
class FakeRestTimer : RestTimer {
    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    override val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    override fun start(totalSeconds: Int) {
        _remainingSeconds.value = totalSeconds
    }

    /** Solo para tests: simula que el descanso terminó. */
    fun complete() {
        _remainingSeconds.value = null
    }
}
