package com.example.fitswap.data.repository.fake

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNotesRepositoryTest {

    @Test
    fun `un ejercicio sin notas expone una lista vacia`() = runTest {
        val repository = FakeNotesRepository()

        val notes = repository.observeNotes("press-de-pecho").first()

        assertTrue(notes.isEmpty())
    }

    @Test
    fun `agregar una nota la refleja en el historial de ese ejercicio`() = runTest {
        val repository = FakeNotesRepository()

        repository.addNote("press-de-pecho", "Ajustar asiento a nivel de barbilla")

        val notes = repository.observeNotes("press-de-pecho").first()
        assertEquals(1, notes.size)
        assertEquals("Ajustar asiento a nivel de barbilla", notes.single().text)
    }

    @Test
    fun `las notas de un ejercicio no afectan las de otro`() = runTest {
        val repository = FakeNotesRepository()

        repository.addNote("press-de-pecho", "Nota de press")
        repository.addNote("sentadilla-hack-prensa", "Nota de sentadilla")

        val pressNotes = repository.observeNotes("press-de-pecho").first()
        val squatNotes = repository.observeNotes("sentadilla-hack-prensa").first()
        assertEquals(listOf("Nota de press"), pressNotes.map { it.text })
        assertEquals(listOf("Nota de sentadilla"), squatNotes.map { it.text })
    }
}
