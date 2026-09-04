package com.example.fitswap.data.di

import com.example.fitswap.timer.AndroidCardioTimerController
import com.example.fitswap.timer.AndroidRestTimerController
import com.example.fitswap.timer.CardioTimer
import com.example.fitswap.timer.RestTimer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {

    @Binds
    abstract fun bindRestTimer(impl: AndroidRestTimerController): RestTimer

    @Binds
    abstract fun bindCardioTimer(impl: AndroidCardioTimerController): CardioTimer
}
