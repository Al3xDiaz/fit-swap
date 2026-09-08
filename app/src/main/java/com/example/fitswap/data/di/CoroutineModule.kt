package com.example.fitswap.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Marca un [CoroutineScope] atado al ciclo de vida de la aplicación, no al de un `ViewModel`.
 * Necesario para mutaciones "fire-and-forget" que deben completarse aunque el usuario navegue
 * fuera de la pantalla que las disparó — `viewModelScope` se cancela apenas se hace back, lo que
 * puede cortar un `insert` a mitad de camino (ver `EditRoutineViewModel`).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
