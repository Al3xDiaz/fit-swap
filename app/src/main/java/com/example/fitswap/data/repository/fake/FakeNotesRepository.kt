package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeNotesRepository @Inject constructor() : NotesRepository {

    private val notesByExercise = MutableStateFlow<Map<String, List<Note>>>(emptyMap())
    private var noteSeq = 0

    override fun observeNotes(exerciseId: String): Flow<List<Note>> =
        notesByExercise.map { it[exerciseId].orEmpty() }

    override fun observeAllNotes(): Flow<List<Note>> =
        notesByExercise.map { it.values.flatten() }

    override suspend fun addNote(exerciseId: String, text: String) {
        val note = Note(id = "note-${noteSeq++}", exerciseId = exerciseId, text = text)
        notesByExercise.update { current ->
            val existing = current[exerciseId].orEmpty()
            current + (exerciseId to (existing + note))
        }
    }

    override suspend fun replaceAllNotes(notes: List<Note>) {
        notesByExercise.value = notes.groupBy { it.exerciseId }
    }
}
