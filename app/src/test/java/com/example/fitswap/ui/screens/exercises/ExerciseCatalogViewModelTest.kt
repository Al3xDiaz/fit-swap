package com.example.fitswap.ui.screens.exercises

import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExerciseCatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `expone el catalogo completo sin busqueda`() = runTest {
        val viewModel = ExerciseCatalogViewModel(FakeExerciseRepository())

        val exercises = viewModel.exercises.first { it.isNotEmpty() }

        assertEquals(ExerciseCatalog.allExercises.size, exercises.size)
    }

    @Test
    fun `actualizar la busqueda filtra el catalogo expuesto`() = runTest {
        val viewModel = ExerciseCatalogViewModel(FakeExerciseRepository())
        viewModel.exercises.first { it.isNotEmpty() }

        viewModel.updateQuery("cinta")

        val exercises = viewModel.exercises.first { it.size == 1 }
        assertEquals(ExerciseCatalog.caminarEnCinta, exercises.single())
    }
}
