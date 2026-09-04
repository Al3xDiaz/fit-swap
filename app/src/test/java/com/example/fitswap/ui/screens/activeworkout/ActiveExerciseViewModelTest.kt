@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import com.example.fitswap.MainDispatcherRule
import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeGalleryRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import com.example.fitswap.data.repository.fake.FakeSettingsRepository
import com.example.fitswap.data.repository.fake.FakeSubstituteRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.timer.FakeRestTimer
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ROUTINE_ID = "default"
private const val PUSH_DAY_ID = "default-martes"
private const val LEGS_DAY_ID = "default-jueves"
private const val SUNDAY_DAY_ID = "default-domingo"

// approachSets = 0, effectiveSets = 4, sin historial -> plan = [WARMUP, EFFECTIVE x4], peso 1kg
private const val ELEVACIONES_ID = "default-martes-elevaciones-laterales"

// segundo ejercicio del día martes, inmediatamente después de ELEVACIONES_ID.
private const val PRESS_MILITAR_ID = "default-martes-press-militar-maquina"

// approachSets = 1, effectiveSets = 4, con historial (60kg) -> plan = [WARMUP, APPROACH, EFFECTIVE x4]
private const val PRESS_DE_PECHO_ID = "default-martes-press-de-pecho"

// último ejercicio del día martes -> no hay siguiente al que auto-avanzar.
// approachSets = 0, effectiveSets = 2 -> plan = [WARMUP, EFFECTIVE, EFFECTIVE]
private const val EXTENSION_TRICEPS_OVERHEAD_ID = "default-martes-extension-triceps-overhead"

// approachSets = 2, effectiveSets = 4, con historial propio (80kg); su sustituto es "prensa-sentadilla-ligera"
private const val SENTADILLA_ID = "default-jueves-sentadilla-hack-prensa"

// approachSets = 0, effectiveSets = 3, sin historial propio; su sustituto "sentadilla-hack-prensa" sí tiene (80kg)
private const val PRENSA_LIGERA_ID = "default-domingo-prensa-sentadilla-ligera"

class ActiveExerciseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        exerciseId: String,
        dayId: String = PUSH_DAY_ID,
        setRepository: FakeSetRepository = FakeSetRepository(),
        historyRepository: HistoryRepository = FakeHistoryRepository(),
        workoutSessionRepository: WorkoutSessionRepository = FakeWorkoutSessionRepository(),
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        galleryRepository: GalleryRepository = FakeGalleryRepository(),
        notesRepository: NotesRepository = FakeNotesRepository(),
        restTimer: FakeRestTimer = FakeRestTimer(),
    ) = ActiveExerciseViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to dayId, "exerciseId" to exerciseId)
        ),
        routineRepository = FakeRoutineRepository(),
        setRepository = setRepository,
        historyRepository = historyRepository,
        substituteRepository = FakeSubstituteRepository(),
        workoutSessionRepository = workoutSessionRepository,
        currentDateProvider = FixedCurrentDateProvider(DayOfWeek.TUESDAY),
        settingsRepository = settingsRepository,
        galleryRepository = galleryRepository,
        notesRepository = notesRepository,
        restTimerController = restTimer,
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

        viewModel.onEffectiveWeightInputChanged("42.5")
        assertEquals(42.5, viewModel.uiState.first().effectiveWeightKg, 0.0)

        viewModel.registerSet()
        viewModel.onEffectiveWeightInputChanged("50.0")
        assertEquals(50.0, viewModel.uiState.first().effectiveWeightKg, 0.0)
    }

    @Test
    fun `cambiar el peso efectivo despues de avanzar no reescribe el peso ya fijado de una etapa pasada`() = runTest {
        val setRepository = FakeSetRepository()
        val viewModel = viewModel(ELEVACIONES_ID, setRepository = setRepository)
        viewModel.uiState.first { !it.isLoading }

        // Warmup weight is fixed at registration time (1kg effective * 0.5 factor = 0.5kg).
        viewModel.registerSet() // warmup — se persiste de inmediato con ese peso
        viewModel.onEffectiveWeightInputChanged("100.0")
        repeat(4) { viewModel.registerSet() } // 4 effective, ya con el peso nuevo -> plan completo
        viewModel.completeExercise() // ya no persiste nada, solo cierra la sesión

        val loggedWarmup = setRepository.observeLoggedSets(ELEVACIONES_ID).first().single { it.type == SetType.WARMUP }
        assertEquals(0.5, loggedWarmup.weightKg, 0.0)
    }

    @Test
    fun `registrar una serie arranca el temporizador de descanso con la duracion configurada`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        settingsRepository.updateRestTimerSeconds(45)
        val viewModel = viewModel(ELEVACIONES_ID, settingsRepository = settingsRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.registerSet()

        val state = viewModel.uiState.first()
        assertNotNull(state.restRemainingSeconds)
        // El timer es fijo por Configuraciones (M9), ya no se deriva del restLabel del ejercicio.
        assertEquals(45, state.restTotalSeconds)
    }

    @Test
    fun `el timer ya no avanza de ejercicio solo al terminar el descanso`() = runTest {
        val restTimer = FakeRestTimer()
        val viewModel = viewModel(ELEVACIONES_ID, restTimer = restTimer)
        viewModel.uiState.first { !it.isLoading }

        repeat(5) { viewModel.registerSet() } // 1 warmup + 4 effective = plan completo
        restTimer.complete()

        val state = viewModel.uiState.first()
        assertTrue(state.isComplete)
        assertNull(state.restRemainingSeconds)
    }

    @Test
    fun `nextExerciseId apunta al siguiente ejercicio del dia`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(PRESS_MILITAR_ID, state.nextExerciseId)
        assertNull(state.previousExerciseId)
    }

    @Test
    fun `previousExerciseId apunta al ejercicio anterior del dia`() = runTest {
        val viewModel = viewModel(PRESS_MILITAR_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ELEVACIONES_ID, state.previousExerciseId)
    }

    @Test
    fun `el ultimo ejercicio del dia no tiene siguiente`() = runTest {
        val viewModel = viewModel(EXTENSION_TRICEPS_OVERHEAD_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertNull(state.nextExerciseId)
    }

    @Test
    fun `hacer swap a un sustituto actualiza el ejercicio activo sin resetear el progreso ya registrado`() = runTest {
        val sessionRepository = FakeWorkoutSessionRepository()
        val viewModel = viewModel(
            SENTADILLA_ID,
            dayId = LEGS_DAY_ID,
            workoutSessionRepository = sessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }

        viewModel.registerSet() // calentamiento ya registrado antes del swap
        val beforeSwap = viewModel.uiState.first()
        assertEquals(SetType.APPROACH, beforeSwap.currentSet?.type)

        sessionRepository.substituteExercise(SENTADILLA_ID, ExerciseCatalog.prensaSentadillaLigera)

        val afterSwap = viewModel.uiState.first { it.exercise?.id == ExerciseCatalog.prensaSentadillaLigera.id }
        assertEquals("Prensa / sentadilla ligera", afterSwap.exercise?.name)
        // El esquema de series (etapa actual, progreso) no se resetea con el swap.
        assertEquals(SetType.APPROACH, afterSwap.currentSet?.type)
    }

    @Test
    fun `el peso sugerido usa el historial de un sustituto cuando el ejercicio propio no tiene historial`() = runTest {
        val viewModel = viewModel(PRENSA_LIGERA_ID, dayId = SUNDAY_DAY_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        // Sin historial propio para "prensa-sentadilla-ligera", cae al de su sustituto "sentadilla-hack-prensa".
        assertEquals(80.0, state.effectiveWeightKg, 0.0)
    }

    @Test
    fun `registrar una serie la persiste de inmediato sin esperar a completar el ejercicio`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(ELEVACIONES_ID, historyRepository = historyRepository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.registerSet() // calentamiento — se persiste de inmediato, no espera a completar

        val history = historyRepository.observeHistory(ExerciseCatalog.elevacionesLaterales.id).first()
        assertEquals(1, history.size)
    }

    @Test
    fun `completar el ejercicio conserva todos los puntos de historial ya persistidos de la sesion`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(ELEVACIONES_ID, historyRepository = historyRepository)
        viewModel.uiState.first { !it.isLoading }

        repeat(5) { viewModel.registerSet() } // 1 warmup + 4 effective -> plan completo
        viewModel.completeExercise()

        val history = historyRepository.observeHistory(ExerciseCatalog.elevacionesLaterales.id).first()
        assertEquals(5, history.size)
        assertEquals(1, history.count { it.type == SetType.WARMUP })
        assertEquals(4, history.count { it.type == SetType.EFFECTIVE })
    }

    @Test
    fun `salir sin completar borra explicitamente el progreso ya persistido de la sesion`() = runTest {
        val historyRepository = FakeHistoryRepository()
        val setRepository = FakeSetRepository()
        val viewModel = viewModel(ELEVACIONES_ID, historyRepository = historyRepository, setRepository = setRepository)
        viewModel.uiState.first { !it.isLoading }

        repeat(3) { viewModel.registerSet() } // progreso parcial ya persistido, nunca se completa
        viewModel.endRoutine() // mismo flujo que "Salir del entrenamiento" con confirmación

        assertTrue(historyRepository.observeHistory(ExerciseCatalog.elevacionesLaterales.id).first().isEmpty())
        assertTrue(setRepository.observeLoggedSets(ELEVACIONES_ID).first().isEmpty())
    }

    @Test
    fun `una serie registrada sobrevive a que se recree el viewmodel sin completar el ejercicio`() = runTest {
        val setRepository = FakeSetRepository()
        val firstViewModel = viewModel(ELEVACIONES_ID, setRepository = setRepository)
        firstViewModel.uiState.first { !it.isLoading }
        firstViewModel.registerSet() // no se completa el ejercicio ni se sale de la rutina

        // Simula que el proceso murió en background (p. ej. al cancelar el timer de descanso
        // desde su notificación) y la pantalla se recrea con un ViewModel nuevo para el mismo
        // ejercicio — la serie ya persistida no debe perderse.
        val recreatedViewModel = viewModel(ELEVACIONES_ID, setRepository = setRepository)
        val state = recreatedViewModel.uiState.first { !it.isLoading }

        assertEquals(SetType.EFFECTIVE, state.currentSet?.type)
        assertEquals(1, state.currentSet?.stageNumber)
    }

    @Test
    fun `las reps se pueden editar directo, ademas de con los botones +-`() = runTest {
        val viewModel = viewModel(ELEVACIONES_ID)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onRepsInputChanged("12")

        assertEquals(12, viewModel.uiState.first().currentReps)
    }

    @Test
    fun `la galeria del ejercicio (agregada desde editar ejercicio) se refleja en el estado`() = runTest {
        // El alta de galería vive en ExerciseFormViewModel — acá solo se observa en modo lectura.
        // Nota: la galería se indexa por el id del Exercise del catálogo (`elevaciones-laterales`),
        // no por el id del RoutineExercise/slot del día (`ELEVACIONES_ID`).
        val galleryRepository = FakeGalleryRepository()
        galleryRepository.addMedia(ExerciseCatalog.elevacionesLaterales.id, "content://media/1", isVideo = false)
        val viewModel = viewModel(ELEVACIONES_ID, galleryRepository = galleryRepository)

        val state = viewModel.uiState.first { it.galleryItems.isNotEmpty() }
        assertEquals(1, state.galleryItems.size)
        assertEquals("content://media/1", state.galleryItems.single().uri)
    }
}
