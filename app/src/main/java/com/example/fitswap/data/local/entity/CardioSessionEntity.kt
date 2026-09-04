package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate

@Entity(
    tableName = "cardio_sessions",
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
data class CardioSessionEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val date: LocalDate,
    val durationSeconds: Int,
    val distanceKm: Double?,
    val avgHeartRate: Int?,
    val calories: Int?,
)

fun CardioSessionEntity.toDomain(): CardioSession = CardioSession(
    id = id,
    exerciseId = exerciseId,
    date = date,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    avgHeartRate = avgHeartRate,
    calories = calories,
)

fun CardioSession.toEntity(): CardioSessionEntity = CardioSessionEntity(
    id = id,
    exerciseId = exerciseId,
    date = date,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    avgHeartRate = avgHeartRate,
    calories = calories,
)
