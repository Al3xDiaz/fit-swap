package com.example.fitswap.data.local.entity

import androidx.room.TypeConverter
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate

class SetTypeConverter {
    @TypeConverter
    fun fromSetType(type: SetType): String = type.name

    @TypeConverter
    fun toSetType(name: String): SetType = SetType.valueOf(name)
}

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(iso: String): LocalDate = LocalDate.parse(iso)
}
