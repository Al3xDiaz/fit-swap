package com.example.fitswap.data.repository.room

import androidx.room.withTransaction
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.dao.CardioSessionDao
import com.example.fitswap.data.local.dao.ExerciseDao
import com.example.fitswap.data.local.dao.GalleryDao
import com.example.fitswap.data.local.dao.HistoryDao
import com.example.fitswap.data.local.dao.NoteDao
import com.example.fitswap.data.local.dao.SubstituteDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.ExerciseRepository
import com.example.fitswap.domain.model.Exercise
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomExerciseRepository @Inject constructor(
    private val database: FitSwapDatabase,
    private val exerciseDao: ExerciseDao,
    private val galleryDao: GalleryDao,
    private val historyDao: HistoryDao,
    private val noteDao: NoteDao,
    private val substituteDao: SubstituteDao,
    private val cardioSessionDao: CardioSessionDao,
) : ExerciseRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeExercises().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getExercise(exerciseId: String): Exercise? =
        exerciseDao.getExercise(exerciseId)?.toDomain()

    override suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insert(exercise.toEntity())
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise.toEntity())
    }

    override suspend fun deleteExercise(exerciseId: String): Boolean {
        if (exerciseDao.routineUsageCount(exerciseId) > 0) return false
        database.withTransaction {
            galleryDao.deleteAllForExercise(exerciseId)
            historyDao.deleteAllForExercise(exerciseId)
            noteDao.deleteAllForExercise(exerciseId)
            substituteDao.deleteAllForExercise(exerciseId)
            cardioSessionDao.deleteAllForExercise(exerciseId)
            exerciseDao.delete(exerciseId)
        }
        return true
    }
}
