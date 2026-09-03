package com.example.fitswap.data.di

import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.data.repository.SubstituteRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.repository.datastore.DataStoreSettingsRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import com.example.fitswap.data.repository.room.RoomExerciseRepository
import com.example.fitswap.data.repository.room.RoomGalleryRepository
import com.example.fitswap.data.repository.room.RoomHistoryRepository
import com.example.fitswap.data.repository.room.RoomNotesRepository
import com.example.fitswap.data.repository.room.RoomRoutineRepository
import com.example.fitswap.data.repository.room.RoomSetRepository
import com.example.fitswap.data.repository.room.RoomSubstituteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Desde M11, la mayoría de los repositorios corren contra Room ([RoomRoutineRepository] y
 * hermanos); desde M12, [SettingsRepository] corre contra DataStore
 * ([DataStoreSettingsRepository]). [WorkoutSessionRepository] queda en memoria a propósito — es
 * estado de la sesión de entrenamiento en curso, no datos persistentes (ver su propio KDoc).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRoutineRepository(impl: RoomRoutineRepository): RoutineRepository

    @Binds
    abstract fun bindSetRepository(impl: RoomSetRepository): SetRepository

    @Binds
    abstract fun bindHistoryRepository(impl: RoomHistoryRepository): HistoryRepository

    @Binds
    abstract fun bindSubstituteRepository(impl: RoomSubstituteRepository): SubstituteRepository

    @Binds
    abstract fun bindWorkoutSessionRepository(impl: FakeWorkoutSessionRepository): WorkoutSessionRepository

    @Binds
    abstract fun bindNotesRepository(impl: RoomNotesRepository): NotesRepository

    @Binds
    abstract fun bindExerciseRepository(impl: RoomExerciseRepository): ExerciseRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindGalleryRepository(impl: RoomGalleryRepository): GalleryRepository
}
