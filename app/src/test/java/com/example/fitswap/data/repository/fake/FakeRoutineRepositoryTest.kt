package com.example.fitswap.data.repository.fake

import com.example.fitswap.data.repository.MoveDirection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val PUSH_DAY_ID = "default-martes"

class FakeRoutineRepositoryTest {

    @Test
    fun `crear una rutina la agrega vacia y sin ser la rutina por defecto`() = runTest {
        val repository = FakeRoutineRepository()

        val newId = repository.createRoutine("Mi rutina")

        val routine = repository.observeRoutine(newId).first()
        assertEquals("Mi rutina", routine?.name)
        assertTrue(routine?.days?.isEmpty() == true)
        assertFalse(routine?.isDefault == true)
    }

    @Test
    fun `agregar un dia lo suma sin afectar los dias existentes`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!

        repository.addDay("default", "Nuevo día")

        val after = repository.observeRoutine("default").first()!!
        assertEquals(before.days.size + 1, after.days.size)
        assertEquals("Nuevo día", after.days.last().name)
        assertTrue(after.days.dropLast(1) == before.days)
    }

    @Test
    fun `renombrar un dia solo afecta ese dia`() = runTest {
        val repository = FakeRoutineRepository()

        repository.renameDay("default", PUSH_DAY_ID, "Push renombrado")

        val routine = repository.observeRoutine("default").first()!!
        assertEquals("Push renombrado", routine.days.first { it.id == PUSH_DAY_ID }.name)
        assertTrue(routine.days.none { it.id != PUSH_DAY_ID && it.name == "Push renombrado" })
    }

    @Test
    fun `eliminar un dia solo quita sus ejercicios, el resto de la rutina queda intacta`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!
        val otherDays = before.days.filterNot { it.id == PUSH_DAY_ID }

        repository.removeDay("default", PUSH_DAY_ID)

        val after = repository.observeRoutine("default").first()!!
        assertTrue(after.days.none { it.id == PUSH_DAY_ID })
        assertEquals(otherDays, after.days)
    }

    @Test
    fun `agregar un ejercicio del catalogo lo suma con un esquema por defecto`() = runTest {
        val repository = FakeRoutineRepository()

        repository.addExerciseToDay("default", PUSH_DAY_ID, ExerciseCatalog.caminarEnCinta.id)

        val day = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }
        val added = day.exercises.last()
        assertEquals(ExerciseCatalog.caminarEnCinta, added.exercise)
        assertEquals(3, added.effectiveSets)
        assertEquals(0, added.approachSets)
    }

    @Test
    fun `agregar un ejercicio ya presente en el dia genera un id distinto`() = runTest {
        val repository = FakeRoutineRepository()

        repository.addExerciseToDay("default", PUSH_DAY_ID, ExerciseCatalog.pressDePecho.id)

        val day = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }
        val ids = day.exercises.map { it.id }
        assertEquals(ids.size, ids.toSet().size) // sin ids duplicados
    }

    @Test
    fun `quitar un ejercicio solo afecta ese ejercicio del dia`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }
        val toRemove = before.exercises[2]

        repository.removeExerciseFromDay("default", PUSH_DAY_ID, toRemove.id)

        val after = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }
        assertEquals(before.exercises.size - 1, after.exercises.size)
        assertEquals(before.exercises.filterNot { it.id == toRemove.id }, after.exercises)
    }

    @Test
    fun `mover un ejercicio hacia abajo lo intercambia con el siguiente preservando el resto del orden`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises

        repository.moveExercise("default", PUSH_DAY_ID, before[0].id, MoveDirection.DOWN)

        val after = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises
        assertEquals(before[1], after[0])
        assertEquals(before[0], after[1])
        assertEquals(before.drop(2), after.drop(2))
    }

    @Test
    fun `mover el primer ejercicio hacia arriba no hace nada`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises

        repository.moveExercise("default", PUSH_DAY_ID, before[0].id, MoveDirection.UP)

        val after = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises
        assertEquals(before, after)
    }

    @Test
    fun `mover el ultimo ejercicio hacia abajo no hace nada`() = runTest {
        val repository = FakeRoutineRepository()
        val before = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises

        repository.moveExercise("default", PUSH_DAY_ID, before.last().id, MoveDirection.DOWN)

        val after = repository.observeRoutine("default").first()!!.days.first { it.id == PUSH_DAY_ID }.exercises
        assertEquals(before, after)
    }

    @Test
    fun `eliminar la rutina por defecto es rechazado`() = runTest {
        val repository = FakeRoutineRepository()

        repository.deleteRoutine("default")

        val remaining = repository.observeRoutines().first()
        assertTrue(remaining.any { it.id == "default" })
    }

    @Test
    fun `eliminar una rutina que no existe no falla`() = runTest {
        val repository = FakeRoutineRepository()

        repository.deleteRoutine("no-existe")

        assertNull(repository.observeRoutine("no-existe").first())
    }
}
