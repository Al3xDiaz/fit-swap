package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import com.example.fitswap.data.repository.fake.FakeCardioSessionRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import com.example.fitswap.data.session.RoutineSessionFinisher
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.data.time.FixedCurrentDateProvider
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.SetType
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ROUTINE_ID = "default"
private const val PUSH_DAY_ID = "default-martes"

// Orden del día (Martes — Push): calentamiento (cardio), elevaciones, press militar, press de
// pecho, aperturas, fondos, extensión cuerda, extensión overhead, vuelta a la calma (cardio).
private const val ELEVACIONES_ID = "default-martes-elevaciones-laterales" // approach=0, effective=4
private const val PRESS_MILITAR_ID = "default-martes-press-militar-maquina" // approach=1, effective=4

class SessionMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        activeExerciseId: String,
        setRepository: FakeSetRepository = FakeSetRepository(),
        workoutSessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
        currentDateProvider: CurrentDateProvider = FixedCurrentDateProvider(DayOfWeek.TUESDAY),
    ) = SessionMenuViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to PUSH_DAY_ID, "exerciseId" to activeExerciseId)
        ),
        routineRepository = FakeRoutineRepository(),
        setRepository = setRepository,
        workoutSessionRepository = workoutSessionRepository,
        currentDateProvider = currentDateProvider,
        routineSessionFinisher = RoutineSessionFinisher(
            setRepository = setRepository,
            historyRepository = FakeHistoryRepository(),
            cardioSessionRepository = FakeCardioSessionRepository(),
            notesRepository = FakeNotesRepository(),
            workoutSessionRepository = workoutSessionRepository,
            currentDateProvider = currentDateProvider,
        ),
    )

    @Test
    fun `el ejercicio activo se marca ACTIVE y el resto sin registros PENDING`() = runTest {
        val viewModel = viewModel(activeExerciseId = ELEVACIONES_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(9, state.items.size) // 7 de fuerza + calentamiento y vuelta a la calma (cardio)
        val elevaciones = state.items.first { it.routineExerciseId == ELEVACIONES_ID }
        val pressMilitar = state.items.first { it.routineExerciseId == PRESS_MILITAR_ID }
        assertEquals(SessionExerciseStatus.ACTIVE, elevaciones.status)
        assertEquals(SessionExerciseStatus.PENDING, pressMilitar.status)
    }

    @Test
    fun `un ejercicio con todas sus series registradas se marca DONE`() = runTest {
        val setRepository = FakeSetRepository()
        val today = FixedCurrentDateProvider(DayOfWeek.TUESDAY).today()
        // Elevaciones laterales: 1 warmup + 4 effective = 5 series para completarlo.
        repeat(5) { index ->
            setRepository.logSet(
                LoggedSet(id = "log-$index", routineExerciseId = ELEVACIONES_ID, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = today)
            )
        }

        val viewModel = viewModel(activeExerciseId = PRESS_MILITAR_ID, setRepository = setRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        val elevaciones = state.items.first { it.routineExerciseId == ELEVACIONES_ID }
        assertEquals(SessionExerciseStatus.DONE, elevaciones.status)
    }

    @Test
    fun `el nombre mostrado usa el sustituto si hubo swap en ese slot`() = runTest {
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(PRESS_MILITAR_ID, ExerciseCatalog.pressDeHombroEnMaquina)

        val viewModel = viewModel(activeExerciseId = ELEVACIONES_ID, workoutSessionRepository = sessionRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        val pressMilitar = state.items.first { it.routineExerciseId == PRESS_MILITAR_ID }
        assertEquals("Press de hombro en máquina", pressMilitar.exerciseName)
    }

    @Test
    fun `guardar rutina limpia las sustituciones de la sesion`() = runTest {
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(PRESS_MILITAR_ID, ExerciseCatalog.pressDeHombroEnMaquina)
        val viewModel = viewModel(activeExerciseId = ELEVACIONES_ID, workoutSessionRepository = sessionRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.saveRoutine()

        val substitution = sessionRepository.observeSubstitution(PRESS_MILITAR_ID).first()
        assertNull(substitution)
    }

    @Test
    fun `descartar rutina limpia las sustituciones y borra las series de hoy`() = runTest {
        val setRepository = FakeSetRepository()
        val today = FixedCurrentDateProvider(DayOfWeek.TUESDAY).today()
        setRepository.logSet(
            LoggedSet(id = "log-0", routineExerciseId = PRESS_MILITAR_ID, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = today)
        )
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(PRESS_MILITAR_ID, ExerciseCatalog.pressDeHombroEnMaquina)
        val viewModel = viewModel(
            activeExerciseId = ELEVACIONES_ID,
            setRepository = setRepository,
            workoutSessionRepository = sessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }

        viewModel.discardRoutine()

        assertNull(sessionRepository.observeSubstitution(PRESS_MILITAR_ID).first())
        assertTrue(setRepository.observeLoggedSets(PRESS_MILITAR_ID, today).first().isEmpty())
    }
}
