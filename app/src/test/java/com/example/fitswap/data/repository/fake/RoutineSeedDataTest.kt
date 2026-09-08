package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineSeedDataTest {

    private val defaultRoutine = seedRoutines().single { it.id == "default" }

    @Test
    fun `cada dia de la rutina por defecto arranca con un calentamiento de cardio`() {
        defaultRoutine.days.forEach { day ->
            val first = day.exercises.first()
            assertTrue("día ${day.id}: el primer ejercicio debería ser CARDIO", first.exercise.type == ExerciseType.CARDIO)
            assertTrue(
                "día ${day.id}: las notas del calentamiento deberían mencionarlo",
                first.notes.orEmpty().contains("Calentamiento")
            )
        }
    }

    @Test
    fun `cada dia de la rutina por defecto termina con una vuelta a la calma de cardio`() {
        defaultRoutine.days.forEach { day ->
            val last = day.exercises.last()
            assertTrue("día ${day.id}: el último ejercicio debería ser CARDIO", last.exercise.type == ExerciseType.CARDIO)
            assertTrue(
                "día ${day.id}: las notas de la vuelta a la calma deberían mencionarlo",
                last.notes.orEmpty().contains("Vuelta a la calma")
            )
        }
    }

    @Test
    fun `cada dia de la rutina por defecto tiene al menos un ejercicio de fuerza ademas del calentamiento y la vuelta a la calma`() {
        defaultRoutine.days.forEach { day ->
            assertTrue("día ${day.id} tiene muy pocos ejercicios: ${day.exercises.size}", day.exercises.size >= 3)
            val strengthExercises = day.exercises.drop(1).dropLast(1)
            assertTrue(
                "día ${day.id}: los ejercicios entre calentamiento y vuelta a la calma deberían ser de fuerza",
                strengthExercises.isNotEmpty() && strengthExercises.all { it.exercise.type == ExerciseType.STRENGTH }
            )
        }
    }

    @Test
    fun `las otras rutinas sembradas no llevan calentamiento ni vuelta a la calma`() {
        // Alcance acotado: solo la rutina por defecto (Push/Pull/Piernas/Full Upper/Domingo) gana
        // calentamiento y vuelta a la calma automáticos; las rutinas de un solo día quedan igual.
        val otherRoutines = seedRoutines().filterNot { it.id == "default" }

        otherRoutines.forEach { routine ->
            routine.days.forEach { day ->
                assertEquals(
                    "día ${day.id} no debería tener ejercicios de cardio",
                    0,
                    day.exercises.count { it.exercise.type == ExerciseType.CARDIO }
                )
            }
        }
    }
}
