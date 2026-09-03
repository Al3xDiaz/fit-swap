package com.example.fitswap.data.di

import android.content.Context
import androidx.room.Room
import com.example.fitswap.data.local.DatabaseSeeder
import com.example.fitswap.data.local.FitSwapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

/**
 * Reemplaza [DatabaseModule] en `androidTest`: base en memoria en vez de en disco, para que cada
 * clase de test arranque con un estado limpio (mismo criterio que ya usaba cada `Fake*Repository`
 * antes de M11 — sin esto, el archivo `fitswap.db` real persistiría entre corridas de
 * `connectedAndroidTest` y los tests dejarían de ser deterministas/aislados entre sí). El seed
 * corre igual, así los tests siguen viendo la misma rutina/catálogo por defecto de siempre.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitSwapDatabase {
        TestRoomExecutor.ensureRegistered()
        val database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java)
            .setQueryExecutor(TestRoomExecutor.executor)
            .setTransactionExecutor(TestRoomExecutor.executor)
            .build()
        runBlocking { DatabaseSeeder(database).seedIfNeeded() }
        return database
    }
}
