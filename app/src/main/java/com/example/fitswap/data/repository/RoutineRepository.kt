package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Routine
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

enum class MoveDirection { UP, DOWN }

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>
    fun observeRoutine(routineId: String): Flow<Routine?>
    suspend fun deleteRoutine(routineId: String)

    /** Crea una rutina vacía (sin días) y devuelve su id. */
    suspend fun createRoutine(name: String): String

    /** Marca [routineId] como la rutina por defecto, desmarcando cualquier otra. */
    suspend fun setDefaultRoutine(routineId: String)

    /** Desmarca la rutina por defecto actual, si hay una. */
    suspend fun clearDefaultRoutine()

    suspend fun addDay(routineId: String, dayName: String, dayOfWeek: DayOfWeek? = null)
    suspend fun renameDay(routineId: String, dayId: String, newName: String)
    suspend fun setDayOfWeek(routineId: String, dayId: String, dayOfWeek: DayOfWeek)
    suspend fun removeDay(routineId: String, dayId: String)

    /**
     * [exerciseId] es el id del catálogo (`Exercise.id`), no de un `RoutineExercise`.
     * Devuelve `false` sin insertar nada si [exerciseId] no existe en el catálogo.
     */
    suspend fun addExerciseToDay(routineId: String, dayId: String, exerciseId: String): Boolean
    suspend fun removeExerciseFromDay(routineId: String, dayId: String, routineExerciseId: String)
    suspend fun moveExercise(routineId: String, dayId: String, routineExerciseId: String, direction: MoveDirection)

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todas las rutinas. */
    suspend fun replaceRoutines(routines: List<Routine>)
}
