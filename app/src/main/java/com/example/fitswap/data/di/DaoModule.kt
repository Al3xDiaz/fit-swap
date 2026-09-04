package com.example.fitswap.data.di

import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.dao.BodyMeasurementDao
import com.example.fitswap.data.local.dao.BodyProfileDao
import com.example.fitswap.data.local.dao.CardioSessionDao
import com.example.fitswap.data.local.dao.ExerciseDao
import com.example.fitswap.data.local.dao.GalleryDao
import com.example.fitswap.data.local.dao.HistoryDao
import com.example.fitswap.data.local.dao.NoteDao
import com.example.fitswap.data.local.dao.RoutineDao
import com.example.fitswap.data.local.dao.SetDao
import com.example.fitswap.data.local.dao.SubstituteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Los DAOs se derivan de [FitSwapDatabase] sin importar si esa base es real o en memoria (test). */
@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun provideExerciseDao(database: FitSwapDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideRoutineDao(database: FitSwapDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideSubstituteDao(database: FitSwapDatabase): SubstituteDao = database.substituteDao()

    @Provides
    fun provideSetDao(database: FitSwapDatabase): SetDao = database.setDao()

    @Provides
    fun provideHistoryDao(database: FitSwapDatabase): HistoryDao = database.historyDao()

    @Provides
    fun provideNoteDao(database: FitSwapDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideGalleryDao(database: FitSwapDatabase): GalleryDao = database.galleryDao()

    @Provides
    fun provideBodyMeasurementDao(database: FitSwapDatabase): BodyMeasurementDao = database.bodyMeasurementDao()

    @Provides
    fun provideBodyProfileDao(database: FitSwapDatabase): BodyProfileDao = database.bodyProfileDao()

    @Provides
    fun provideCardioSessionDao(database: FitSwapDatabase): CardioSessionDao = database.cardioSessionDao()
}
