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

const val REST_TIMER_PROGRESS_CHANNEL_ID = "rest_timer_progress"
const val REST_TIMER_DONE_CHANNEL_ID = "rest_timer_done"
private const val PROGRESS_NOTIFICATION_ID = 1001
private const val DONE_NOTIFICATION_ID = 1002
private const val ACTION_CANCEL = "com.example.fitswap.timer.ACTION_CANCEL"

/**
 * Foreground service que corre el descanso entre series independiente de la pantalla/ViewModel
 * (ver [RestTimerController]): mientras cuenta, muestra una notificación silenciosa persistente
 * con el tiempo restante; al llegar a cero dispara una notificación con sonido en un canal
 * separado y se detiene.
 */
@AndroidEntryPoint
class RestTimerService : Service() {

    @Inject lateinit var restTimerController: AndroidRestTimerController

    private val scope = CoroutineScope(SupervisorJob())
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            cancelCountdown()
            return START_NOT_STICKY
        }
        val totalSeconds = intent?.getIntExtra(EXTRA_TOTAL_SECONDS, 0) ?: 0
        startCountdown(totalSeconds)
        return START_NOT_STICKY
    }

    /** Cancela el descanso en curso desde la acción "Cancelar" de la notificación — mismo
     * cierre que al llegar a cero, pero sin disparar la notificación de "descanso terminado". */
    private fun cancelCountdown() {
        tickJob?.cancel()
        restTimerController.updateRemaining(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startCountdown(totalSeconds: Int) {
        tickJob?.cancel()
        startForeground(PROGRESS_NOTIFICATION_ID, progressNotification(totalSeconds))
        restTimerController.updateRemaining(totalSeconds)
        tickJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                restTimerController.updateRemaining(remaining)
                notificationManager.notify(PROGRESS_NOTIFICATION_ID, progressNotification(totalSeconds, remaining))
            }
            restTimerController.updateRemaining(null)
            notificationManager.notify(DONE_NOTIFICATION_ID, doneNotification())
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    private fun progressNotification(totalSeconds: Int, remainingSeconds: Int = totalSeconds): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return NotificationCompat.Builder(this, REST_TIMER_PROGRESS_CHANNEL_ID)
            .setContentTitle("Descanso entre series")
            .setContentText("%02d:%02d".format(minutes, seconds))
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", cancelIntent())
            .build()
    }

    private fun doneNotification(): Notification =
        NotificationCompat.Builder(this, REST_TIMER_DONE_CHANNEL_ID)
            .setContentTitle("¡Descanso terminado!")
            .setContentText("Es hora de la siguiente serie")
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setAutoCancel(true)
            .build()

    /** Notificaciones sin `setContentIntent`: tocarlas no hace nada — solo se pueden accionar
     * con el botón "Cancelar" de la notificación de progreso. */
    private fun cancelIntent(): PendingIntent = PendingIntent.getService(
        this,
        0,
        Intent(this, RestTimerService::class.java).setAction(ACTION_CANCEL),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
