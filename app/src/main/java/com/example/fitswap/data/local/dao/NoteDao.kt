package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitswap.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE exerciseId = :exerciseId")
    fun observeNotes(exerciseId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun clearAll()

    @Query("DELETE FROM notes WHERE exerciseId = :exerciseId")
    suspend fun deleteAllForExercise(exerciseId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Transaction
    suspend fun replaceAll(notes: List<NoteEntity>) {
        clearAll()
        insertAll(notes)
    }
}
