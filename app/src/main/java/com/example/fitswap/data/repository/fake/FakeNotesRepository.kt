package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.domain.model.Note
import com.example.fitswap.domain.model.noteId
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeNotesRepository @Inject constructor() : NotesRepository {

    private val notesByExercise = MutableStateFlow<Map<String, List<Note>>>(emptyMap())

    override fun observeNotes(exerciseId: String): Flow<List<Note>> =
        notesByExercise.map { it[exerciseId].orEmpty() }

    override fun observeNote(exerciseId: String, date: LocalDate): Flow<Note?> =
        notesByExercise.map { current -> current[exerciseId].orEmpty().find { it.id == noteId(exerciseId, date) } }

    override fun observeAllNotes(): Flow<List<Note>> =
        notesByExercise.map { it.values.flatten() }

    override suspend fun setNote(exerciseId: String, date: LocalDate, text: String) {
        val note = Note(id = noteId(exerciseId, date), exerciseId = exerciseId, date = date, text = text)
        notesByExercise.update { current ->
            val existing = current[exerciseId].orEmpty().filterNot { it.id == note.id }
            current + (exerciseId to (existing + note))
        }
    }

    override suspend fun replaceAllNotes(notes: List<Note>) {
        notesByExercise.value = notes.groupBy { it.exerciseId }
    }

    override suspend fun deleteNote(exerciseId: String, date: LocalDate) {
        val id = noteId(exerciseId, date)
        notesByExercise.update { current ->
            val existing = current[exerciseId].orEmpty().filterNot { it.id == id }
            current + (exerciseId to existing)
        }
    }

    override suspend fun deleteAllNotesForDate(date: LocalDate) {
        notesByExercise.update { current ->
            current.mapValues { (_, notes) -> notes.filterNot { it.date == date } }
        }
    }
}
