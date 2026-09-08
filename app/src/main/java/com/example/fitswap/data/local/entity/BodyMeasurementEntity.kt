package com.example.fitswap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitswap.domain.model.BodyMeasurementEntry
import java.time.LocalDate

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val weightKg: Double,
    val neckCm: Double?,
    val waistCm: Double?,
    val hipCm: Double?,
    val chestCm: Double?,
    val armCm: Double?,
    val legCm: Double?,
    val calfCm: Double?,
    val gluteCm: Double?,
    val forearmCm: Double?,
    val shoulderCm: Double?,
    val wristCm: Double?,
)

fun BodyMeasurementEntity.toDomain(): BodyMeasurementEntry = BodyMeasurementEntry(
    id = id,
    date = date,
    weightKg = weightKg,
    neckCm = neckCm,
    waistCm = waistCm,
    hipCm = hipCm,
    chestCm = chestCm,
    armCm = armCm,
    legCm = legCm,
    calfCm = calfCm,
    gluteCm = gluteCm,
    forearmCm = forearmCm,
    shoulderCm = shoulderCm,
    wristCm = wristCm,
)

fun BodyMeasurementEntry.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    id = id,
    date = date,
    weightKg = weightKg,
    neckCm = neckCm,
    waistCm = waistCm,
    hipCm = hipCm,
    chestCm = chestCm,
    armCm = armCm,
    legCm = legCm,
    calfCm = calfCm,
    gluteCm = gluteCm,
    forearmCm = forearmCm,
    shoulderCm = shoulderCm,
    wristCm = wristCm,
)
