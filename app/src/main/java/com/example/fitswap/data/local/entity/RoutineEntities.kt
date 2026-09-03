package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.domain.model.RoutineExercise
import java.time.DayOfWeek

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDefault: Boolean,
)

@Entity(
    tableName = "routine_days",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("routineId")],
)
data class RoutineDayEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val name: String,
    /** Nombre del enum [DayOfWeek], o nulo para días de rutinas de un solo día. */
    val dayOfWeek: String?,
    val orderIndex: Int,
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
        ),
    ],
    indices = [Index("dayId"), Index("exerciseId")],
)
data class RoutineExerciseEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val exerciseId: String,
    val approachSets: Int,
    val effectiveSets: Int,
    val effectiveRepsLabel: String,
    val approachGuideline: String?,
    val restLabel: String,
    val usesStraps: Boolean,
    val notes: String?,
    val orderIndex: Int,
)

fun RoutineExerciseEntity.toDomain(exercise: Exercise): RoutineExercise = RoutineExercise(
    id = id,
    exercise = exercise,
    approachSets = approachSets,
    effectiveSets = effectiveSets,
    effectiveRepsLabel = effectiveRepsLabel,
    approachGuideline = approachGuideline,
    restLabel = restLabel,
    usesStraps = usesStraps,
    notes = notes,
)

fun RoutineDayEntity.toDomain(exercises: List<RoutineExercise>): RoutineDay = RoutineDay(
    id = id,
    name = name,
    exercises = exercises,
    dayOfWeek = dayOfWeek?.let { DayOfWeek.valueOf(it) },
)

fun RoutineEntity.toDomain(days: List<RoutineDay>): Routine = Routine(
    id = id,
    name = name,
    days = days,
    isDefault = isDefault,
)

/** Todas las [Exercise] embebidas en los `RoutineExercise` de una rutina, sin duplicados por id. */
fun Routine.embeddedExercises(): List<Exercise> =
    days.flatMap { day -> day.exercises.map { it.exercise } }.distinctBy { it.id }

/**
 * Aplana una [Routine] a sus tres tablas normalizadas, calculando `orderIndex` desde el orden de
 * las listas — usado tanto por el seed inicial (`DatabaseSeeder`) como por `replaceRoutines`
 * (import de Herramientas), para no repetir esta lógica dos veces.
 */
fun Routine.toEntities(): Triple<RoutineEntity, List<RoutineDayEntity>, List<RoutineExerciseEntity>> {
    val routineEntity = RoutineEntity(id = id, name = name, isDefault = isDefault)
    val dayEntities = mutableListOf<RoutineDayEntity>()
    val routineExerciseEntities = mutableListOf<RoutineExerciseEntity>()
    days.forEachIndexed { dayIndex, day ->
        dayEntities += RoutineDayEntity(
            id = day.id,
            routineId = id,
            name = day.name,
            dayOfWeek = day.dayOfWeek?.name,
            orderIndex = dayIndex,
        )
        day.exercises.forEachIndexed { exerciseIndex, routineExercise ->
            routineExerciseEntities += RoutineExerciseEntity(
                id = routineExercise.id,
                dayId = day.id,
                exerciseId = routineExercise.exercise.id,
                approachSets = routineExercise.approachSets,
                effectiveSets = routineExercise.effectiveSets,
                effectiveRepsLabel = routineExercise.effectiveRepsLabel,
                approachGuideline = routineExercise.approachGuideline,
                restLabel = routineExercise.restLabel,
                usesStraps = routineExercise.usesStraps,
                notes = routineExercise.notes,
                orderIndex = exerciseIndex,
            )
        }
    }
    return Triple(routineEntity, dayEntities, routineExerciseEntities)
}
