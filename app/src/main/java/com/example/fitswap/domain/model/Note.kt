package com.example.fitswap.domain.model

import java.time.LocalDate
import kotlinx.serialization.Serializable

/**
 * Una nota por [Exercise] y día (`exerciseId` + `date`) — no una lista libre: escribir una nota
 * el mismo día para el mismo ejercicio reemplaza la anterior (ver [id], derivado de ambos). Se
 * muestra junto a la sesión de ese día en el historial del ejercicio.
 */
@Serializable
data class Note(
    val id: String,
    val exerciseId: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val text: String,
)

fun noteId(exerciseId: String, date: LocalDate): String = "$exerciseId|$date"
