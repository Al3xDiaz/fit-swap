package com.example.fitswap

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.runner.AndroidJUnitRunner
import com.example.fitswap.timer.createFitSwapNotificationChannels
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    // HiltTestApplication no ejecuta FitSwapApp.onCreate(), así que los canales de notificación
    // de los timers nunca se crean en tests instrumentados — sin esto, cualquier test que llegue
    // al foreground service del descanso/cardio crashea con "invalid channel for service
    // notification" (ver com.example.fitswap.timer.createFitSwapNotificationChannels).
    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) app.createFitSwapNotificationChannels()
    }
}
