package com.example.fitswap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitswap.data.local.dao.BodyMeasurementDao
import com.example.fitswap.data.local.dao.BodyProfileDao
import com.example.fitswap.data.local.dao.ExerciseDao
import com.example.fitswap.data.local.dao.GalleryDao
import com.example.fitswap.data.local.dao.HistoryDao
import com.example.fitswap.data.local.dao.NoteDao
import com.example.fitswap.data.local.dao.RoutineDao
import com.example.fitswap.data.local.dao.SetDao
import com.example.fitswap.data.local.dao.SubstituteDao
import com.example.fitswap.data.local.entity.BodyMeasurementEntity
import com.example.fitswap.data.local.entity.BodyProfileEntity
import com.example.fitswap.data.local.entity.ExerciseEntity
import com.example.fitswap.data.local.entity.GalleryItemEntity
import com.example.fitswap.data.local.entity.HistoryPointEntity
import com.example.fitswap.data.local.entity.LocalDateConverter
import com.example.fitswap.data.local.entity.LoggedSetEntity
import com.example.fitswap.data.local.entity.NoteEntity
import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import com.example.fitswap.data.local.entity.SetTypeConverter
import com.example.fitswap.data.local.entity.StringListConverter
import com.example.fitswap.data.local.entity.SubstituteLinkEntity

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        SubstituteLinkEntity::class,
        LoggedSetEntity::class,
        HistoryPointEntity::class,
        NoteEntity::class,
        GalleryItemEntity::class,
        BodyMeasurementEntity::class,
        BodyProfileEntity::class,
    ],
    // v2 (M14): agrega body_measurements/body_profile. Sin migración porque todavía no hay
    // usuarios reales — ver `fallbackToDestructiveMigration()` en DatabaseModule.
    version = 2,
    exportSchema = false,
)
@TypeConverters(StringListConverter::class, SetTypeConverter::class, LocalDateConverter::class)
abstract class FitSwapDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun substituteDao(): SubstituteDao
    abstract fun setDao(): SetDao
    abstract fun historyDao(): HistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun galleryDao(): GalleryDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun bodyProfileDao(): BodyProfileDao
}
