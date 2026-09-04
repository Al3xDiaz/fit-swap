package com.example.fitswap.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ACTION_CARDIO_START = "com.example.fitswap.timer.ACTION_CARDIO_START"
const val ACTION_CARDIO_PAUSE = "com.example.fitswap.timer.ACTION_CARDIO_PAUSE"
const val ACTION_CARDIO_RESUME = "com.example.fitswap.timer.ACTION_CARDIO_RESUME"
const val ACTION_CARDIO_STOP = "com.example.fitswap.timer.ACTION_CARDIO_STOP"

/**
 * Implementación real de [CardioTimer]: cada acción arranca [CardioTimerService] (para que el
 * conteo y las notificaciones cada 10 minutos sobrevivan a que la app se minimice); es el propio
 * Service quien va publicando el estado acá mientras corre, no el ViewModel — mismo criterio que
 * [AndroidRestTimerController].
 */
@Singleton
class AndroidCardioTimerController @Inject constructor(
    @ApplicationContext private val context: Context,
) : CardioTimer {
    private val _state = MutableStateFlow(CardioTimerState(CardioTimerStatus.STOPPED, 0))
    override val state: StateFlow<CardioTimerState> = _state.asStateFlow()

    override fun start() = sendAction(ACTION_CARDIO_START)
    override fun pause() = sendAction(ACTION_CARDIO_PAUSE)
    override fun resume() = sendAction(ACTION_CARDIO_RESUME)
    override fun stop() = sendAction(ACTION_CARDIO_STOP)

    private fun sendAction(action: String) {
        val intent = Intent(context, CardioTimerService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, intent)
    }

    /** Llamado únicamente por [CardioTimerService] para publicar el estado actual. */
    internal fun updateState(newState: CardioTimerState) {
        _state.value = newState
    }
}
