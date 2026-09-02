package com.example.fitswap.domain.logic

import com.example.fitswap.data.repository.fake.ExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCatalogSearchTest {

    private val all = ExerciseCatalog.allExercises

    @Test
    fun `una consulta en blanco devuelve el catalogo completo`() {
        val result = ExerciseCatalogSearch.filter(all, "   ")

        assertEquals(all, result)
    }

    @Test
    fun `filtra por nombre sin importar mayusculas`() {
        val result = ExerciseCatalogSearch.filter(all, "PRESS de PECHO")

        assertEquals(listOf(ExerciseCatalog.pressDePecho), result)
    }

    @Test
    fun `filtra por grupo muscular`() {
        val result = ExerciseCatalogSearch.filter(all, "bíceps")

        val expected = setOf(ExerciseCatalog.curlBicepsBarra, ExerciseCatalog.curlMartillo, ExerciseCatalog.curlBicepsAlternado)
        assertEquals(expected, result.toSet())
    }

    @Test
    fun `filtra por equipo`() {
        val result = ExerciseCatalogSearch.filter(all, "cinta")

        assertEquals(listOf(ExerciseCatalog.caminarEnCinta), result)
    }

    @Test
    fun `filtra por tag`() {
        val result = ExerciseCatalogSearch.filter(all, "calentamiento")

        assertTrue(result.contains(ExerciseCatalog.caminarEnCinta))
    }

    @Test
    fun `una consulta sin coincidencias devuelve lista vacia`() {
        val result = ExerciseCatalogSearch.filter(all, "no existe este ejercicio")

        assertTrue(result.isEmpty())
    }
}
