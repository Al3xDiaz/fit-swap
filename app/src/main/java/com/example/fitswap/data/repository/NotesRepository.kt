package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(exerciseId: String): Flow<List<Note>>
    suspend fun addNote(exerciseId: String, text: String)

    /** Notas de todos los ejercicios combinadas — usado por Herramientas (M9) para exportar. */
    fun observeAllNotes(): Flow<List<Note>>

    /** Usado por Herramientas (M9) al importar un backup: reemplaza todas las notas. */
    suspend fun replaceAllNotes(notes: List<Note>)
}
