package com.example.fitswap.data.di

import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.data.time.SystemCurrentDateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo separado de [RepositoryModule] (no un `@Binds` más ahí) para que los tests instrumentados
 * puedan reemplazar solo esta fuente de "hoy" vía `@TestInstallIn` sin tocar el resto de los
 * bindings — necesario para que pantallas que dependen del día de la semana actual (Detalle de
 * rutina) sean deterministas en `androidTest` sin importar en qué fecha real corra el test.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DateModule {

    @Binds
    abstract fun bindCurrentDateProvider(impl: SystemCurrentDateProvider): CurrentDateProvider
}
