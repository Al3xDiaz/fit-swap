package com.example.fitswap.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.io.File
import javax.inject.Singleton

/**
 * Reemplaza [SettingsDataStoreModule] en `androidTest`: un archivo temporal nuevo por clase de test
 * (mismo criterio que [TestDatabaseModule] con Room en memoria) — sin esto, el archivo real de
 * DataStore persistiría entre corridas de `connectedAndroidTest` y los tests dejarían de ser
 * deterministas/aislados entre sí.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SettingsDataStoreModule::class])
object TestSettingsDataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { File.createTempFile("test_settings", ".preferences_pb", context.cacheDir) }
        )
}
