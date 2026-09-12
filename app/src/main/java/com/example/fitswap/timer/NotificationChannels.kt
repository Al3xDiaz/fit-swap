package com.example.fitswap.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi

/** Dos canales por timer (progreso/hito) a propósito: el de progreso es silencioso (se actualiza
 * cada segundo, no debe sonar en cada tick), el otro sí suena — es la alerta que el usuario
 * necesita aunque tenga la app minimizada. Los canales solo existen desde Android 8 (API 26); en
 * versiones anteriores `NotificationCompat.Builder` no los necesita.
 *
 * Extraído de [com.example.fitswap.FitSwapApp] para poder invocarse también desde
 * `HiltTestRunner`: los tests instrumentados corren sobre `HiltTestApplication`, que no ejecuta
 * `FitSwapApp.onCreate()` — sin esto, los foreground services de los timers crashean con
 * "invalid channel for service notification" apenas un test los arranca. */
@RequiresApi(Build.VERSION_CODES.O)
fun Context.createFitSwapNotificationChannels() {
    val notificationManager = getSystemService(NotificationManager::class.java)

    val progressChannel = NotificationChannel(
        REST_TIMER_PROGRESS_CHANNEL_ID,
        "Descanso en curso",
        NotificationManager.IMPORTANCE_LOW,
    ).apply {
        description = "Muestra el tiempo restante del descanso entre series"
        setSound(null, null)
    }

    val doneChannel = NotificationChannel(
        REST_TIMER_DONE_CHANNEL_ID,
        "Descanso terminado",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Avisa cuando termina el descanso entre series"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        setSound(soundUri, audioAttributes)
    }

    val cardioProgressChannel = NotificationChannel(
        CARDIO_PROGRESS_CHANNEL_ID,
        "Cardio en curso",
        NotificationManager.IMPORTANCE_LOW,
    ).apply {
        description = "Muestra el tiempo transcurrido del timer de cardio"
        setSound(null, null)
    }

    val cardioMilestoneChannel = NotificationChannel(
        CARDIO_MILESTONE_CHANNEL_ID,
        "Progreso de cardio",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Avisa cada 10 minutos mientras el timer de cardio está corriendo"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        setSound(soundUri, audioAttributes)
    }

    notificationManager.createNotificationChannels(
        listOf(progressChannel, doneChannel, cardioProgressChannel, cardioMilestoneChannel)
    )
}
