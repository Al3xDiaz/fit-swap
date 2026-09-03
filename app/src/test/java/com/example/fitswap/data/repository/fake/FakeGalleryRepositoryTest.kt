package com.example.fitswap.data.repository.fake

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeGalleryRepositoryTest {

    @Test
    fun `un ejercicio sin medios expone una lista vacia`() = runTest {
        val repository = FakeGalleryRepository()

        val items = repository.observeGallery("press-de-pecho").first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun `agregar un medio lo refleja en la galeria de ese ejercicio`() = runTest {
        val repository = FakeGalleryRepository()

        repository.addMedia("press-de-pecho", "content://media/1", isVideo = false)

        val items = repository.observeGallery("press-de-pecho").first()
        assertEquals(1, items.size)
        assertEquals("content://media/1", items.single().uri)
        assertTrue(!items.single().isVideo)
    }

    @Test
    fun `agregar un medio a un ejercicio no afecta la galeria de otro`() = runTest {
        val repository = FakeGalleryRepository()

        repository.addMedia("press-de-pecho", "content://media/1", isVideo = false)
        repository.addMedia("sentadilla-hack-prensa", "content://media/2", isVideo = true)

        val pressItems = repository.observeGallery("press-de-pecho").first()
        val squatItems = repository.observeGallery("sentadilla-hack-prensa").first()
        assertEquals(listOf("content://media/1"), pressItems.map { it.uri })
        assertEquals(listOf("content://media/2"), squatItems.map { it.uri })
    }
}
