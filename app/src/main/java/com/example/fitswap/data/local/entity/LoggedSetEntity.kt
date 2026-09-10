package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate

@Entity(
    tableName = "logged_sets",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("routineExerciseId")],
)
data class LoggedSetEntity(
    @PrimaryKey val id: String,
    val routineExerciseId: String,
    val type: SetType,
    val reps: Int,
    val weightKg: Double,
    val date: LocalDate,
)

fun LoggedSetEntity.toDomain(): LoggedSet = LoggedSet(
    id = id,
    routineExerciseId = routineExerciseId,
    type = type,
    reps = reps,
    weightKg = weightKg,
    date = date,
)

fun LoggedSet.toEntity(): LoggedSetEntity = LoggedSetEntity(
    id = id,
    routineExerciseId = routineExerciseId,
    type = type,
    reps = reps,
    weightKg = weightKg,
    date = date,
)
