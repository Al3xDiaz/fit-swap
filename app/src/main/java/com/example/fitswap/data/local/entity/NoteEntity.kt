package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.Note

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("exerciseId")],
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val text: String,
)

fun NoteEntity.toDomain(): Note = Note(id = id, exerciseId = exerciseId, text = text)

fun Note.toEntity(): NoteEntity = NoteEntity(id = id, exerciseId = exerciseId, text = text)
