package com.example.fitswap.data.repository

import com.example.fitswap.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(exerciseId: String): Flow<List<Note>>
    suspend fun addNote(exerciseId: String, text: String)
}
