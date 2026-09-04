package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.ExerciseType

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val tags: List<String>,
    val type: ExerciseType = ExerciseType.STRENGTH,
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    equipment = equipment,
    tags = tags,
    type = type,
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    equipment = equipment,
    tags = tags,
    type = type,
)

/** [Exercise.tags] es casi siempre vacía o de un elemento — se guarda como CSV, no como tabla aparte. */
class StringListConverter {
    @TypeConverter
    fun fromList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toList(csv: String): List<String> = if (csv.isEmpty()) emptyList() else csv.split(",")
}
