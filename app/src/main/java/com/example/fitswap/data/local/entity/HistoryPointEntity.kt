package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate

@Entity(
    tableName = "history_points",
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
data class HistoryPointEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val date: LocalDate,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
)

fun HistoryPointEntity.toDomain(): HistoryPoint = HistoryPoint(
    id = id,
    exerciseId = exerciseId,
    date = date,
    type = type,
    reps = reps,
    weightKg = weightKg,
)

fun HistoryPoint.toEntity(): HistoryPointEntity = HistoryPointEntity(
    id = id,
    exerciseId = exerciseId,
    date = date,
    type = type,
    reps = reps,
    weightKg = weightKg,
)
