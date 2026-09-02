package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import com.example.fitswap.domain.model.SetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ROUTINE_ID = "default"
private const val PUSH_DAY_ID = "default-martes"

// approachSets = 0, effectiveSets = 4, sin historial -> plan = [WARMUP, EFFECTIVE x4], peso 1kg
private const val ELEVACIONES_ID = "default-martes-elevaciones-laterales"

// approachSets = 1, effectiveSets = 4, con historial (60kg) -> plan = [WARMUP, APPROACH, EFFECTIVE x4]
private const val PRESS_DE_PECHO_ID = "default-martes-press-de-pecho"

class ActiveExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(exerciseId: String) = ActiveExerciseViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to PUSH_DAY_ID, "exerciseId" to exerciseId)
        ),
        routineRepository = FakeRoutineRepository(),
        setRepository = FakeSetRepository(),
        historyRepository = FakeHistoryRepository(),
    )

    @Test
    fun `peso efectivo sugerido es 1kg cuando el ejercicio no tiene historial`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(1.0, state.effectiveWeightKg, 0.0)
        assertEquals(null, state.lastLoggedWeightKg)
    }

    @Test
    fun `peso efectivo sugerido es el ultimo registrado cuando el ejercicio tiene historial`() = runTest {
        val viewModel = viewModel(PRESS_DE_PECHO_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(60.0, state.effectiveWeightKg, 0.0)
        assertEquals(60.0, state.lastLoggedWeightKg)
    }

    @Test
    fun `el avance entre tipos de serie es lineal, una serie a la vez sin saltar`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)
        viewModel.uiState.first { !it.isLoading }

        val initial = viewModel.uiState.first()
        assertEquals(SetType.WARMUP, initial.currentSet?.type)

        viewModel.registerSet()
        val afterWarmup = viewModel.uiState.first()
        assertEquals(SetType.EFFECTIVE, afterWarmup.currentSet?.type)
        assertEquals(1, afterWarmup.currentSet?.stageNumber)
        assertEquals(0, afterWarmup.effectiveSetsDone)

        viewModel.registerSet()
        val afterFirstEffective = viewModel.uiState.first()
        assertEquals(SetType.EFFECTIVE, afterFirstEffective.currentSet?.type)
        assertEquals(2, afterFirstEffective.currentSet?.stageNumber)
        assertEquals(1, afterFirstEffective.effectiveSetsDone)
    }

    @Test
    fun `registrar todas las series marca el ejercicio como completo`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)
        viewModel.uiState.first { !it.isLoading }

        repeat(5) { viewModel.registerSet() } // 1 warmup + 4 effective

        val state = viewModel.uiState.first()
        assertTrue(state.isComplete)
        assertEquals(null, state.currentSet)
        assertEquals(4, state.effectiveSetsDone)
    }

    @Test
    fun `peso efectivo es editable en cualquier punto del flujo`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)
        viewModel.uiState.first { !it.isLoading }

        viewModel.updateEffectiveWeight(42.5)
        assertEquals(42.5, viewModel.uiState.first().effectiveWeightKg, 0.0)

        viewModel.registerSet()
        viewModel.updateEffectiveWeight(50.0)
        assertEquals(50.0, viewModel.uiState.first().effectiveWeightKg, 0.0)
    }

    @Test
    fun `cambiar el peso efectivo despues de avanzar no reescribe el peso ya fijado de una etapa pasada`() = runTest {
        val setRepository = FakeSetRepository()
        val viewModel = ActiveExerciseViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("routineId" to ROUTINE_ID, "dayId" to PUSH_DAY_ID, "exerciseId" to ELEVACIONES_ID)
            ),
            routineRepository = FakeRoutineRepository(),
            setRepository = setRepository,
            historyRepository = FakeHistoryRepository(),
        )
        viewModel.uiState.first { !it.isLoading }

        // Warmup weight is fixed at registration time (1kg effective * 0.5 factor = 0.5kg).
        viewModel.registerSet()
        viewModel.updateEffectiveWeight(100.0)

        val loggedWarmup = setRepository.observeLoggedSets(ELEVACIONES_ID).first().single()
        assertEquals(SetType.WARMUP, loggedWarmup.type)
        assertEquals(0.5, loggedWarmup.weightKg, 0.0)
    }

    @Test
    fun `registrar una serie arranca el temporizador de descanso con la duracion del ejercicio`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)
        viewModel.uiState.first { !it.isLoading }

        viewModel.registerSet()

        val state = viewModel.uiState.first()
        assertNotNull(state.restRemainingSeconds)
        assertEquals(60, state.restTotalSeconds) // "60–75 s" -> primer numero, 60
    }
}
