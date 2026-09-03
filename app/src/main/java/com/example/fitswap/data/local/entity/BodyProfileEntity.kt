package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyProfile

/** Fila única (mismo criterio "dato casi-singleton" que Configuraciones, M12) — [id] siempre
 * [SINGLETON_ID]; guardar hace upsert sobre esa misma fila. */
@Entity(tableName = "body_profile")
data class BodyProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val biologicalSex: String?,
    val heightCm: Double?,
)

const val SINGLETON_ID = 0

fun BodyProfileEntity.toDomain(): BodyProfile = BodyProfile(
    biologicalSex = biologicalSex?.let { BiologicalSex.valueOf(it) },
    heightCm = heightCm,
)

fun BodyProfile.toEntity(): BodyProfileEntity = BodyProfileEntity(
    id = SINGLETON_ID,
    biologicalSex = biologicalSex?.name,
    heightCm = heightCm,
)
