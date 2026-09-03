package com.example.fitswap.data.di

import android.content.Context
import androidx.room.Room
import com.example.fitswap.data.local.DatabaseSeeder
import com.example.fitswap.data.local.FitSwapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

private const val DATABASE_NAME = "fitswap.db"

/**
 * Separado de [DaoModule] a propósito: es la única pieza que `androidTest` necesita reemplazar
 * (`TestDatabaseModule`, base en memoria en vez de en disco) — los `@Provides` de los DAOs no
 * cambian entre producción y test, así que viven en un módulo aparte que no hace falta duplicar.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitSwapDatabase {
        val database = Room.databaseBuilder(context, FitSwapDatabase::class.java, DATABASE_NAME)
            // Todavía no hay usuarios reales ni estrategia de migración — ante un cambio de schema
            // (como el de M14), recrear la base desde cero es preferible a una migración manual
            // que nadie necesita todavía.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        // Seed una sola vez, al primer arranque real (count() > 0 en cualquier arranque siguiente
        // hace que esto sea prácticamente instantáneo) — bloquear acá evita que cualquier
        // repositorio llegue a leer una tabla vacía antes de que el seed termine.
        runBlocking { DatabaseSeeder(database).seedIfNeeded() }
        return database
    }
}
