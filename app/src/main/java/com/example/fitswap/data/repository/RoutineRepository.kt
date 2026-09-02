package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Routine
import kotlinx.coroutines.flow.Flow

enum class MoveDirection { UP, DOWN }

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>
    fun observeRoutine(routineId: String): Flow<Routine?>
    suspend fun deleteRoutine(routineId: String)

    /** Crea una rutina vacía (sin días) y devuelve su id. */
    suspend fun createRoutine(name: String): String

    suspend fun addDay(routineId: String, dayName: String)
    suspend fun renameDay(routineId: String, dayId: String, newName: String)
    suspend fun removeDay(routineId: String, dayId: String)

    /** [exerciseId] es el id del catálogo (`Exercise.id`), no de un `RoutineExercise`. */
    suspend fun addExerciseToDay(routineId: String, dayId: String, exerciseId: String)
    suspend fun removeExerciseFromDay(routineId: String, dayId: String, routineExerciseId: String)
    suspend fun moveExercise(routineId: String, dayId: String, routineExerciseId: String, direction: MoveDirection)

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todas las rutinas. */
    suspend fun replaceRoutines(routines: List<Routine>)
}
