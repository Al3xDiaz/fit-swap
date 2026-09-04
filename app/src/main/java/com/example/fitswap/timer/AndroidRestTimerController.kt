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

const val EXTRA_TOTAL_SECONDS = "totalSeconds"

/**
 * Implementación real de [RestTimer]: arrancar el descanso acá arranca [RestTimerService] (para
 * que el conteo y la notificación final sobrevivan a que la app se minimice); es el propio Service
 * quien va publicando el estado acá mientras corre, no el ViewModel.
 */
@Singleton
class AndroidRestTimerController @Inject constructor(
    @ApplicationContext private val context: Context,
) : RestTimer {
    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    override val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    override fun start(totalSeconds: Int) {
        val intent = Intent(context, RestTimerService::class.java)
            .putExtra(EXTRA_TOTAL_SECONDS, totalSeconds)
        ContextCompat.startForegroundService(context, intent)
    }

    /** Llamado únicamente por [RestTimerService] para publicar el conteo restante. */
    internal fun updateRemaining(seconds: Int?) {
        _remainingSeconds.value = seconds
    }
}
