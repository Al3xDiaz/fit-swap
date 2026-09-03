package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Membresía de un ejercicio a un grupo de sustitutos (misma idea que los grupos hardcodeados de
 * `FakeSubstituteRepository`): todo ejercicio con el mismo [groupId] es sustituto de los demás.
 */
@Entity(
    tableName = "substitute_links",
    primaryKeys = ["exerciseId", "groupId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("exerciseId"), Index("groupId")],
)
data class SubstituteLinkEntity(
    val exerciseId: String,
    val groupId: String,
)
