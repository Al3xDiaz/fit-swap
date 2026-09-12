package com.example.fitswap

import android.app.Application
import android.os.Build
import com.example.fitswap.timer.createFitSwapNotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitSwapApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createFitSwapNotificationChannels()
    }
}
