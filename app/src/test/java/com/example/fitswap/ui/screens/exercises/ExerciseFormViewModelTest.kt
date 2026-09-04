package com.example.fitswap.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeExerciseRepository
import com.example.fitswap.data.repository.fake.FakeGalleryRepository
import com.example.fitswap.domain.model.ExerciseType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private fun viewModel(
    exerciseId: String,
    exerciseRepository: FakeExerciseRepository = FakeExerciseRepository(),
) = ExerciseFormViewModel(
    savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId)),
    exerciseRepository = exerciseRepository,
    galleryRepository = FakeGalleryRepository(),
)

class ExerciseFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `un ejercicio nuevo es de tipo fuerza por defecto`() = runTest {
        val viewModel = viewModel(NEW_EXERCISE_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ExerciseType.STRENGTH, state.type)
    }

    @Test
    fun `marcar un ejercicio como cardio y guardar persiste el tipo`() = runTest {
        val exerciseRepository = FakeExerciseRepository()
        val viewModel = viewModel(NEW_EXERCISE_ID, exerciseRepository = exerciseRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onNameChanged("Caminar en cinta")
        viewModel.onMuscleGroupChanged("Cardio")
        viewModel.onEquipmentChanged("Cinta")
        viewModel.onTypeChanged(ExerciseType.CARDIO)
        val saved = viewModel.save()

        assertEquals(true, saved)
        val persisted = exerciseRepository.observeExercises().first().first { it.name == "Caminar en cinta" }
        assertEquals(ExerciseType.CARDIO, persisted.type)
    }

    @Test
    fun `editar un ejercicio existente carga su tipo actual`() = runTest {
        val exerciseRepository = FakeExerciseRepository()
        val existing = exerciseRepository.observeExercises().first().first { it.type == ExerciseType.CARDIO }
        val viewModel = viewModel(existing.id, exerciseRepository = exerciseRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ExerciseType.CARDIO, state.type)
    }
}
