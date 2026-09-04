package com.example.fitswap.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitswap.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val CARDIO_PROGRESS_CHANNEL_ID = "cardio_timer_progress"
const val CARDIO_MILESTONE_CHANNEL_ID = "cardio_timer_milestone"
private const val PROGRESS_NOTIFICATION_ID = 2001
private const val MILESTONE_NOTIFICATION_ID = 2002
private const val MILESTONE_INTERVAL_SECONDS = 600 // 10 minutos

/**
 * Foreground service del timer de cardio, análogo a [RestTimerService] pero contando hacia
 * arriba desde 0 y con control manual (a diferencia del descanso, no se auto-inicia): mientras
 * corre, muestra una notificación silenciosa persistente con el tiempo transcurrido, y cada
 * [MILESTONE_INTERVAL_SECONDS] dispara una notificación con sonido ("10 min completados 🙌", "20
 * min completados 🙌", etc.). El guardado real de la sesión lo hace el ViewModel al tocar
 * "Registrar sesión" — este service no persiste nada.
 */
@AndroidEntryPoint
class CardioTimerService : Service() {

    @Inject lateinit var cardioTimerController: AndroidCardioTimerController

    private val scope = CoroutineScope(SupervisorJob())
    private var tickJob: Job? = null
    private var elapsedSeconds = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CARDIO_PAUSE -> pause()
            ACTION_CARDIO_RESUME -> resume()
            ACTION_CARDIO_STOP -> stopTimer()
            else -> start()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        elapsedSeconds = 0
        startForeground(PROGRESS_NOTIFICATION_ID, progressNotification(CardioTimerStatus.RUNNING))
        cardioTimerController.updateState(CardioTimerState(CardioTimerStatus.RUNNING, elapsedSeconds))
        runTickLoop()
    }

    private fun resume() {
        notificationManager.notify(PROGRESS_NOTIFICATION_ID, progressNotification(CardioTimerStatus.RUNNING))
        cardioTimerController.updateState(CardioTimerState(CardioTimerStatus.RUNNING, elapsedSeconds))
        runTickLoop()
    }

    private fun runTickLoop() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1_000)
                elapsedSeconds++
                cardioTimerController.updateState(CardioTimerState(CardioTimerStatus.RUNNING, elapsedSeconds))
                notificationManager.notify(PROGRESS_NOTIFICATION_ID, progressNotification(CardioTimerStatus.RUNNING))
                if (elapsedSeconds % MILESTONE_INTERVAL_SECONDS == 0) {
                    notificationManager.notify(MILESTONE_NOTIFICATION_ID, milestoneNotification(elapsedSeconds))
                }
            }
        }
    }

    /** Pausa sin resetear [elapsedSeconds] ni quitar la notificación — solo detiene el conteo;
     * el service sigue en foreground (mismo criterio que el botón "Cancelar" del descanso, pero
     * acá "Pausar" no cierra la sesión, la deja retomable con "Reanudar"). */
    private fun pause() {
        tickJob?.cancel()
        notificationManager.notify(PROGRESS_NOTIFICATION_ID, progressNotification(CardioTimerStatus.PAUSED))
        cardioTimerController.updateState(CardioTimerState(CardioTimerStatus.PAUSED, elapsedSeconds))
    }

    /** Detiene el timer pero conserva [elapsedSeconds] en el estado publicado — la pantalla lo
     * usa para habilitar "Registrar sesión" con la duración final; el próximo [start] sí resetea
     * a 0 para una sesión nueva. */
    private fun stopTimer() {
        tickJob?.cancel()
        cardioTimerController.updateState(CardioTimerState(CardioTimerStatus.STOPPED, elapsedSeconds))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    private fun progressNotification(status: CardioTimerStatus): Notification {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val builder = NotificationCompat.Builder(this, CARDIO_PROGRESS_CHANNEL_ID)
            .setContentTitle(if (status == CardioTimerStatus.PAUSED) "Cardio en pausa" else "Cardio en curso")
            .setContentText("%02d:%02d".format(minutes, seconds))
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        if (status == CardioTimerStatus.PAUSED) {
            builder.addAction(android.R.drawable.ic_media_play, "Reanudar", actionIntent(ACTION_CARDIO_RESUME))
        } else {
            builder.addAction(android.R.drawable.ic_media_pause, "Pausar", actionIntent(ACTION_CARDIO_PAUSE))
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", actionIntent(ACTION_CARDIO_STOP))
        return builder.build()
    }

    private fun milestoneNotification(elapsed: Int): Notification {
        val minutes = elapsed / 60
        return NotificationCompat.Builder(this, CARDIO_MILESTONE_CHANNEL_ID)
            .setContentTitle("$minutes min completados 🙌")
            .setContentText("Seguí así — el timer sigue corriendo")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setAutoCancel(true)
            .build()
    }

    /** Notificaciones sin `setContentIntent`: tocarlas no hace nada — solo se accionan con los
     * botones "Pausar"/"Reanudar"/"Detener" de la notificación de progreso. */
    private fun actionIntent(action: String): PendingIntent = PendingIntent.getService(
        this,
        action.hashCode(),
        Intent(this, CardioTimerService::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
