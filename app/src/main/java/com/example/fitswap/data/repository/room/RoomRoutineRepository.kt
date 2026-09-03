package com.example.fitswap.data.repository.room

import androidx.room.withTransaction
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.dao.ExerciseDao
import com.example.fitswap.data.local.dao.RoutineDao
import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import com.example.fitswap.data.local.entity.embeddedExercises
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntities
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.MoveDirection
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private const val DEFAULT_NEW_EXERCISE_APPROACH_SETS = 0
private const val DEFAULT_NEW_EXERCISE_EFFECTIVE_SETS = 3
private const val DEFAULT_NEW_EXERCISE_REPS_LABEL = "8–12"
private const val DEFAULT_NEW_EXERCISE_REST_LABEL = "90 s"

@Singleton
class RoomRoutineRepository @Inject constructor(
    private val database: FitSwapDatabase,
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao,
) : RoutineRepository {

    override fun observeRoutines(): Flow<List<Routine>> = combine(
        routineDao.observeRoutines(),
        routineDao.observeDays(),
        routineDao.observeRoutineExercises(),
        exerciseDao.observeExercises(),
    ) { routines, days, routineExercises, exercises ->
        val exerciseById = exercises.associateBy { it.id }
        val exercisesByDay = routineExercises.groupBy { it.dayId }
        val daysByRoutine = days.groupBy { it.routineId }

        routines.map { routineEntity ->
            val routineDays = daysByRoutine[routineEntity.id].orEmpty().sortedBy { it.orderIndex }.map { dayEntity ->
                val dayExercises = exercisesByDay[dayEntity.id].orEmpty()
                    .sortedBy { it.orderIndex }
                    .mapNotNull { reEntity -> exerciseById[reEntity.exerciseId]?.let { reEntity.toDomain(it.toDomain()) } }
                dayEntity.toDomain(dayExercises)
            }
            routineEntity.toDomain(routineDays)
        }
    }

    override fun observeRoutine(routineId: String): Flow<Routine?> =
        observeRoutines().map { routines -> routines.find { it.id == routineId } }

    override suspend fun deleteRoutine(routineId: String) {
        routineDao.deleteRoutine(routineId)
    }

    override suspend fun createRoutine(name: String): String {
        val newId = nextSequentialId("routine", routineDao.allRoutineIds())
        routineDao.insertRoutine(RoutineEntity(id = newId, name = name, isDefault = false))
        return newId
    }

    override suspend fun addDay(routineId: String, dayName: String) {
        val newDayId = nextSequentialId("$routineId-dia", routineDao.dayIdsForRoutine(routineId))
        val orderIndex = routineDao.maxDayOrderIndex(routineId) + 1
        routineDao.insertDay(
            RoutineDayEntity(id = newDayId, routineId = routineId, name = dayName, dayOfWeek = null, orderIndex = orderIndex)
        )
    }

    override suspend fun renameDay(routineId: String, dayId: String, newName: String) {
        routineDao.renameDay(dayId, newName)
    }

    override suspend fun removeDay(routineId: String, dayId: String) {
        routineDao.removeDay(dayId)
    }

    override suspend fun addExerciseToDay(routineId: String, dayId: String, exerciseId: String) {
        val exercise = exerciseDao.getExercise(exerciseId) ?: return
        val existing = routineDao.getExercisesForDay(dayId)
        val newId = uniqueRoutineExerciseId(dayId, exercise.id, existing.map { it.id })
        val orderIndex = routineDao.maxExerciseOrderIndex(dayId) + 1
        routineDao.insertRoutineExercise(
            RoutineExerciseEntity(
                id = newId,
                dayId = dayId,
                exerciseId = exercise.id,
                approachSets = DEFAULT_NEW_EXERCISE_APPROACH_SETS,
                effectiveSets = DEFAULT_NEW_EXERCISE_EFFECTIVE_SETS,
                effectiveRepsLabel = DEFAULT_NEW_EXERCISE_REPS_LABEL,
                approachGuideline = null,
                restLabel = DEFAULT_NEW_EXERCISE_REST_LABEL,
                usesStraps = false,
                notes = null,
                orderIndex = orderIndex,
            )
        )
    }

    override suspend fun removeExerciseFromDay(routineId: String, dayId: String, routineExerciseId: String) {
        routineDao.removeRoutineExercise(routineExerciseId)
    }

    override suspend fun moveExercise(routineId: String, dayId: String, routineExerciseId: String, direction: MoveDirection) {
        database.withTransaction {
            val exercises = routineDao.getExercisesForDay(dayId)
            val index = exercises.indexOfFirst { it.id == routineExerciseId }
            val targetIndex = if (direction == MoveDirection.UP) index - 1 else index + 1
            if (index < 0 || targetIndex !in exercises.indices) return@withTransaction

            val current = exercises[index]
            val target = exercises[targetIndex]
            routineDao.updateRoutineExercises(
                listOf(current.copy(orderIndex = target.orderIndex), target.copy(orderIndex = current.orderIndex))
            )
        }
    }

    override suspend fun replaceRoutines(routines: List<Routine>) {
        database.withTransaction {
            val allExercises = routines.flatMap { it.embeddedExercises() }.distinctBy { it.id }
            exerciseDao.insertAll(allExercises.map { it.toEntity() })

            val routineEntities = mutableListOf<RoutineEntity>()
            val dayEntities = mutableListOf<RoutineDayEntity>()
            val routineExerciseEntities = mutableListOf<RoutineExerciseEntity>()
            routines.forEach { routine ->
                val (routineEntity, days, routineExercises) = routine.toEntities()
                routineEntities += routineEntity
                dayEntities += days
                routineExerciseEntities += routineExercises
            }
            routineDao.replaceAll(routineEntities, dayEntities, routineExerciseEntities)
        }
    }

    private fun uniqueRoutineExerciseId(dayId: String, exerciseId: String, existingIds: List<String>): String {
        val base = "$dayId-$exerciseId"
        if (base !in existingIds) return base
        var suffix = 2
        while ("$base-$suffix" in existingIds) suffix++
        return "$base-$suffix"
    }

    /**
     * `"$prefix-0"`, `"$prefix-1"`, ... — mismo formato predecible que ya usaba
     * `FakeRoutineRepository` (un contador en memoria), pero derivado de lo que ya existe en la
     * tabla en cada llamada en vez de un campo `var` en la clase: un contador en memoria se
     * reiniciaría en 0 en cada arranque del proceso y podría volver a generar un id que ya existe
     * en el disco de una sesión anterior.
     */
    private fun nextSequentialId(prefix: String, existingIds: List<String>): String {
        val suffixRegex = Regex("^${Regex.escape(prefix)}-(\\d+)$")
        val maxUsed = existingIds.mapNotNull { suffixRegex.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull() }.maxOrNull() ?: -1
        return "$prefix-${maxUsed + 1}"
    }
}
