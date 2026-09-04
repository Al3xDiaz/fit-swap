package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.MoveDirection
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.domain.model.RoutineExercise
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val DEFAULT_NEW_EXERCISE_APPROACH_SETS = 0
private const val DEFAULT_NEW_EXERCISE_EFFECTIVE_SETS = 3
private const val DEFAULT_NEW_EXERCISE_REPS_LABEL = "8–12"
private const val DEFAULT_NEW_EXERCISE_REST_LABEL = "90 s"

@Singleton
class FakeRoutineRepository @Inject constructor() : RoutineRepository {

    private val routines = MutableStateFlow(seedRoutines())
    private var routineSeq = 0
    private var daySeq = 0

    override fun observeRoutines(): Flow<List<Routine>> = routines.asStateFlow()

    override fun observeRoutine(routineId: String): Flow<Routine?> =
        routines.map { list -> list.find { it.id == routineId } }

    override suspend fun deleteRoutine(routineId: String) {
        routines.update { list -> list.filterNot { it.id == routineId && !it.isDefault } }
    }

    override suspend fun createRoutine(name: String): String {
        val newId = "routine-${routineSeq++}"
        routines.update { it + Routine(id = newId, name = name, days = emptyList(), isDefault = false) }
        return newId
    }

    override suspend fun setDefaultRoutine(routineId: String) {
        routines.update { list -> list.map { it.copy(isDefault = it.id == routineId) } }
    }

    override suspend fun clearDefaultRoutine() {
        routines.update { list -> list.map { it.copy(isDefault = false) } }
    }

    override suspend fun addDay(routineId: String, dayName: String, dayOfWeek: DayOfWeek?) {
        val newDayId = "$routineId-dia-${daySeq++}"
        updateRoutine(routineId) { routine ->
            routine.copy(
                days = routine.days + RoutineDay(
                    id = newDayId,
                    name = dayName,
                    exercises = emptyList(),
                    dayOfWeek = dayOfWeek,
                )
            )
        }
    }

    override suspend fun renameDay(routineId: String, dayId: String, newName: String) {
        updateDay(routineId, dayId) { day -> day.copy(name = newName) }
    }

    override suspend fun setDayOfWeek(routineId: String, dayId: String, dayOfWeek: DayOfWeek) {
        updateDay(routineId, dayId) { day -> day.copy(dayOfWeek = dayOfWeek) }
    }

    override suspend fun removeDay(routineId: String, dayId: String) {
        updateRoutine(routineId) { routine -> routine.copy(days = routine.days.filterNot { it.id == dayId }) }
    }

    override suspend fun addExerciseToDay(routineId: String, dayId: String, exerciseId: String) {
        val exercise = ExerciseCatalog.allExercises.find { it.id == exerciseId } ?: return
        updateDay(routineId, dayId) { day ->
            val newRoutineExercise = RoutineExercise(
                id = uniqueRoutineExerciseId(day, exercise.id),
                exercise = exercise,
                approachSets = DEFAULT_NEW_EXERCISE_APPROACH_SETS,
                effectiveSets = DEFAULT_NEW_EXERCISE_EFFECTIVE_SETS,
                effectiveRepsLabel = DEFAULT_NEW_EXERCISE_REPS_LABEL,
                restLabel = DEFAULT_NEW_EXERCISE_REST_LABEL,
            )
            day.copy(exercises = day.exercises + newRoutineExercise)
        }
    }

    override suspend fun removeExerciseFromDay(routineId: String, dayId: String, routineExerciseId: String) {
        updateDay(routineId, dayId) { day ->
            day.copy(exercises = day.exercises.filterNot { it.id == routineExerciseId })
        }
    }

    override suspend fun moveExercise(routineId: String, dayId: String, routineExerciseId: String, direction: MoveDirection) {
        updateDay(routineId, dayId) { day ->
            val index = day.exercises.indexOfFirst { it.id == routineExerciseId }
            val targetIndex = if (direction == MoveDirection.UP) index - 1 else index + 1
            if (index < 0 || targetIndex !in day.exercises.indices) return@updateDay day

            val reordered = day.exercises.toMutableList()
            val moved = reordered.removeAt(index)
            reordered.add(targetIndex, moved)
            day.copy(exercises = reordered)
        }
    }

    override suspend fun replaceRoutines(routines: List<Routine>) {
        this.routines.value = routines
    }

    private fun uniqueRoutineExerciseId(day: RoutineDay, exerciseId: String): String {
        val base = "${day.id}-$exerciseId"
        if (day.exercises.none { it.id == base }) return base
        var suffix = 2
        while (day.exercises.any { it.id == "$base-$suffix" }) suffix++
        return "$base-$suffix"
    }

    private fun updateRoutine(routineId: String, transform: (Routine) -> Routine) {
        routines.update { list -> list.map { routine -> if (routine.id == routineId) transform(routine) else routine } }
    }

    private fun updateDay(routineId: String, dayId: String, transform: (RoutineDay) -> RoutineDay) {
        updateRoutine(routineId) { routine ->
            routine.copy(days = routine.days.map { day -> if (day.id == dayId) transform(day) else day })
        }
    }
}
