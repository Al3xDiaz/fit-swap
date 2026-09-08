package com.example.fitswap.data.repository.room

import com.example.fitswap.data.local.dao.NoteDao
import com.example.fitswap.data.local.entity.toDomain
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.domain.model.Note
import com.example.fitswap.domain.model.noteId
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomNotesRepository @Inject constructor(private val noteDao: NoteDao) : NotesRepository {

    override fun observeNotes(exerciseId: String): Flow<List<Note>> =
        noteDao.observeNotes(exerciseId).map { entities -> entities.map { it.toDomain() } }

    override fun observeNote(exerciseId: String, date: LocalDate): Flow<Note?> =
        noteDao.observeNoteById(noteId(exerciseId, date)).map { it?.toDomain() }

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes().map { entities -> entities.map { it.toDomain() } }

    override suspend fun setNote(exerciseId: String, date: LocalDate, text: String) {
        val note = Note(id = noteId(exerciseId, date), exerciseId = exerciseId, date = date, text = text)
        noteDao.insert(note.toEntity())
    }

    override suspend fun deleteNote(exerciseId: String, date: LocalDate) {
        noteDao.deleteById(noteId(exerciseId, date))
    }

    override suspend fun replaceAllNotes(notes: List<Note>) {
        noteDao.replaceAll(notes.map { it.toEntity() })
    }
}
