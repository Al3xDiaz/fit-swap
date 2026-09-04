package com.example.fitswap

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitswap.timer.CARDIO_MILESTONE_CHANNEL_ID
import com.example.fitswap.timer.CARDIO_PROGRESS_CHANNEL_ID
import com.example.fitswap.timer.REST_TIMER_DONE_CHANNEL_ID
import com.example.fitswap.timer.REST_TIMER_PROGRESS_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitSwapApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannels()
    }

    /** Dos canales separados a propósito: el de progreso es silencioso (se actualiza cada
     * segundo, no debe sonar en cada tick), el de "descanso terminado" sí suena — es la alerta
     * que el usuario necesita aunque tenga la app minimizada. Los canales solo existen desde
     * Android 8 (API 26); en versiones anteriores `NotificationCompat.Builder` no los necesita. */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
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
}
