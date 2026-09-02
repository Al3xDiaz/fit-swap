package com.example.fitswap.data.repository.fake

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSubstituteRepositoryTest {

    private val repository = FakeSubstituteRepository()

    @Test
    fun `los sustitutos de un ejercicio no lo incluyen a si mismo`() = runTest {
        val substitutes = repository.substitutesFor(ExerciseCatalog.pressDePecho.id)

        assertTrue(substitutes.none { it.id == ExerciseCatalog.pressDePecho.id })
        assertTrue(substitutes.any { it.id == ExerciseCatalog.pressInclinado.id })
    }

    @Test
    fun `la relacion de sustitutos es simetrica dentro de un grupo`() = runTest {
        val fromA = repository.substitutesFor(ExerciseCatalog.remoConBarraOMancuerna.id).map { it.id }
        val fromB = repository.substitutesFor(ExerciseCatalog.remoChestSupported.id).map { it.id }

        assertTrue(ExerciseCatalog.remoChestSupported.id in fromA)
        assertTrue(ExerciseCatalog.remoConBarraOMancuerna.id in fromB)
    }

    @Test
    fun `un ejercicio de aislamiento sin grupo curado no tiene sustitutos`() = runTest {
        val substitutes = repository.substitutesFor(ExerciseCatalog.elevacionesLaterales.id)

        assertEquals(emptyList<Any>(), substitutes)
    }
}
