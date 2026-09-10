package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Note
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    /** Todas las notas de un ejercicio (una por día en que se haya escrito una) — usada para
     * mostrarlas junto a cada sesión del historial. */
    fun observeNotes(exerciseId: String): Flow<List<Note>>

    /** La nota de [exerciseId] para [date] puntual, o nula si no se escribió ninguna ese día —
     * es lo que edita `NotesScreen` para la sesión en curso. */
    fun observeNote(exerciseId: String, date: LocalDate): Flow<Note?>

    /** Upsert por (exerciseId, date): escribir de nuevo el mismo día reemplaza la nota anterior
     * de ese día en vez de acumular una lista. */
    suspend fun setNote(exerciseId: String, date: LocalDate, text: String)

    /** Borra la nota de [exerciseId] para [date], si existe — usado al eliminar un registro
     * completo del historial de ese día. */
    suspend fun deleteNote(exerciseId: String, date: LocalDate)

    /** Borra las notas de TODOS los ejercicios en una fecha — usado al descartar toda la sesión
     * de hoy al terminar la rutina (ver `RoutineSessionFinisher`). */
    suspend fun deleteAllNotesForDate(date: LocalDate)

    /** Notas de todos los ejercicios combinadas — usado por Herramientas (M9) para exportar. */
    fun observeAllNotes(): Flow<List<Note>>

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todas las notas. */
    suspend fun replaceAllNotes(notes: List<Note>)
}
