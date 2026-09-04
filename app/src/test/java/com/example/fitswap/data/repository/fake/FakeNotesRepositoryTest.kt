package com.example.fitswap.data.repository.fake

import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private val DAY_1 = LocalDate.of(2026, 9, 1)
private val DAY_2 = LocalDate.of(2026, 9, 2)

class FakeNotesRepositoryTest {

    @Test
    fun `un ejercicio sin notas expone una lista vacia`() = runTest {
        val repository = FakeNotesRepository()

        val notes = repository.observeNotes("press-de-pecho").first()

        assertTrue(notes.isEmpty())
    }

    @Test
    fun `escribir una nota la refleja en el historial de ese ejercicio`() = runTest {
        val repository = FakeNotesRepository()

        repository.setNote("press-de-pecho", DAY_1, "Ajustar asiento a nivel de barbilla")

        val notes = repository.observeNotes("press-de-pecho").first()
        assertEquals(1, notes.size)
        assertEquals("Ajustar asiento a nivel de barbilla", notes.single().text)
    }

    @Test
    fun `escribir de nuevo el mismo dia reemplaza la nota anterior de ese dia`() = runTest {
        val repository = FakeNotesRepository()

        repository.setNote("press-de-pecho", DAY_1, "Primera nota")
        repository.setNote("press-de-pecho", DAY_1, "Nota corregida")

        val notes = repository.observeNotes("press-de-pecho").first()
        assertEquals(listOf("Nota corregida"), notes.map { it.text })
    }

    @Test
    fun `dias distintos conviven como notas separadas`() = runTest {
        val repository = FakeNotesRepository()

        repository.setNote("press-de-pecho", DAY_1, "Nota del día 1")
        repository.setNote("press-de-pecho", DAY_2, "Nota del día 2")

        val notes = repository.observeNotes("press-de-pecho").first()
        assertEquals(2, notes.size)
        assertEquals("Nota del día 1", repository.observeNote("press-de-pecho", DAY_1).first()?.text)
        assertEquals("Nota del día 2", repository.observeNote("press-de-pecho", DAY_2).first()?.text)
    }

    @Test
    fun `las notas de un ejercicio no afectan las de otro`() = runTest {
        val repository = FakeNotesRepository()

        repository.setNote("press-de-pecho", DAY_1, "Nota de press")
        repository.setNote("sentadilla-hack-prensa", DAY_1, "Nota de sentadilla")

        val pressNotes = repository.observeNotes("press-de-pecho").first()
        val squatNotes = repository.observeNotes("sentadilla-hack-prensa").first()
        assertEquals(listOf("Nota de press"), pressNotes.map { it.text })
        assertEquals(listOf("Nota de sentadilla"), squatNotes.map { it.text })
    }

    @Test
    fun `un dia sin nota expone nulo`() = runTest {
        val repository = FakeNotesRepository()

        assertNull(repository.observeNote("press-de-pecho", DAY_1).first())
    }
}
