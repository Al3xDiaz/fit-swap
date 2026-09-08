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
import com.example.fitswap.data.repository.fake.FakeCardioSessionRepository
import com.example.fitswap.data.repository.fake.FakeGalleryRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeRoutineRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import com.example.fitswap.data.repository.fake.FakeSettingsRepository
import com.example.fitswap.data.repository.fake.FakeSubstituteRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import com.example.fitswap.domain.model.ExerciseType
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.timer.CardioTimerStatus
import com.example.fitswap.timer.FakeCardioTimer
import com.example.fitswap.timer.FakeRestTimer
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ROUTINE_ID = "default"
private const val PUSH_DAY_ID = "default-martes"
private const val LEGS_DAY_ID = "default-jueves"
private const val SUNDAY_DAY_ID = "default-domingo"

// Cardio, primer ejercicio del día (calentamiento agregado a la rutina sembrada) — sin previousExerciseId.
private const val CALENTAMIENTO_ID = "default-martes-calentamiento"

// approachSets = 0, effectiveSets = 4, sin historial -> plan = [WARMUP, EFFECTIVE x4], peso 1kg
private const val ELEVACIONES_ID = "default-martes-elevaciones-laterales"

// segundo ejercicio del día martes, inmediatamente después de ELEVACIONES_ID.
private const val PRESS_MILITAR_ID = "default-martes-press-militar-maquina"

// approachSets = 1, effectiveSets = 4, con historial (60kg) -> plan = [WARMUP, APPROACH, EFFECTIVE x4]
private const val PRESS_DE_PECHO_ID = "default-martes-press-de-pecho"

// último ejercicio de fuerza del día martes -> su siguiente es la vuelta a la calma (cardio).
// approachSets = 0, effectiveSets = 2 -> plan = [WARMUP, EFFECTIVE, EFFECTIVE]
private const val EXTENSION_TRICEPS_OVERHEAD_ID = "default-martes-extension-triceps-overhead"

// Cardio, último ejercicio del día (vuelta a la calma agregada a la rutina sembrada) -> sin siguiente.
private const val VUELTA_A_LA_CALMA_ID = "default-martes-vuelta-a-la-calma"

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
        routineRepository: FakeRoutineRepository = FakeRoutineRepository(),
        setRepository: FakeSetRepository = FakeSetRepository(),
        historyRepository: HistoryRepository = FakeHistoryRepository(),
        workoutSessionRepository: WorkoutSessionRepository = FakeWorkoutSessionRepository(),
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        galleryRepository: GalleryRepository = FakeGalleryRepository(),
        notesRepository: NotesRepository = FakeNotesRepository(),
        restTimer: FakeRestTimer = FakeRestTimer(),
        cardioSessionRepository: FakeCardioSessionRepository = FakeCardioSessionRepository(),
        cardioTimer: FakeCardioTimer = FakeCardioTimer(),
    ) = ActiveExerciseViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("routineId" to ROUTINE_ID, "dayId" to dayId, "exerciseId" to exerciseId)
        ),
        routineRepository = routineRepository,
        setRepository = setRepository,
        historyRepository = historyRepository,
        substituteRepository = FakeSubstituteRepository(),
        workoutSessionRepository = workoutSessionRepository,
        currentDateProvider = FixedCurrentDateProvider(DayOfWeek.TUESDAY),
        settingsRepository = settingsRepository,
        galleryRepository = galleryRepository,
        notesRepository = notesRepository,
        restTimerController = restTimer,
        cardioSessionRepository = cardioSessionRepository,
        cardioTimerController = cardioTimer,
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
        // El día ahora arranca con el calentamiento (cardio) agregado a la rutina sembrada.
        assertEquals(CALENTAMIENTO_ID, state.previousExerciseId)
    }

    @Test
    fun `el calentamiento del dia no tiene ejercicio anterior`() = runTest {
        val viewModel = viewModel(CALENTAMIENTO_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ELEVACIONES_ID, state.nextExerciseId)
        assertNull(state.previousExerciseId)
    }

    @Test
    fun `previousExerciseId apunta al ejercicio anterior del dia`() = runTest {
        val viewModel = viewModel(PRESS_MILITAR_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ELEVACIONES_ID, state.previousExerciseId)
    }

    @Test
    fun `el ultimo ejercicio de fuerza del dia tiene la vuelta a la calma como siguiente`() = runTest {
        val viewModel = viewModel(EXTENSION_TRICEPS_OVERHEAD_ID)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(VUELTA_A_LA_CALMA_ID, state.nextExerciseId)
    }

    @Test
    fun `la vuelta a la calma es el ultimo ejercicio del dia, sin siguiente`() = runTest {
        val viewModel = viewModel(VUELTA_A_LA_CALMA_ID)

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
    fun `terminar la rutina conservando el progreso no borra las series ya registradas, a diferencia de endRoutine`() = runTest {
        val setRepository = FakeSetRepository()
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(PRESS_MILITAR_ID, ExerciseCatalog.pressDeHombroEnMaquina)
        val viewModel = viewModel(
            EXTENSION_TRICEPS_OVERHEAD_ID,
            setRepository = setRepository,
            workoutSessionRepository = sessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }

        repeat(2) { viewModel.registerSet() } // progreso a medias del ultimo ejercicio de fuerza, nunca se completa
        viewModel.finishRoutineKeepingProgress()

        assertTrue(setRepository.observeLoggedSets(EXTENSION_TRICEPS_OVERHEAD_ID).first().isNotEmpty())
        assertNull(sessionRepository.observeSubstitution(PRESS_MILITAR_ID).first())
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

    /** Agrega "Caminar en cinta" (único ejercicio CARDIO del catálogo) a un día vacío de prueba y
     * devuelve el id de `RoutineExercise` resultante, listo para pasar como `exerciseId` a
     * [viewModel]. */
    private suspend fun cardioExerciseId(routineRepository: FakeRoutineRepository, dayId: String): String {
        routineRepository.addExerciseToDay(ROUTINE_ID, dayId, ExerciseCatalog.caminarEnCinta.id)
        return "$dayId-${ExerciseCatalog.caminarEnCinta.id}"
    }

    @Test
    fun `un ejercicio de tipo cardio no usa el plan de series de peso`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val id = cardioExerciseId(routineRepository, SUNDAY_DAY_ID)
        val viewModel = viewModel(id, dayId = SUNDAY_DAY_ID, routineRepository = routineRepository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(ExerciseType.CARDIO, state.exerciseType)
        assertNull(state.currentSet)
        assertTrue(state.phases.isEmpty())
        assertNotNull(state.cardioUiState)
        assertEquals(CardioTimerStatus.STOPPED, state.cardioUiState?.timerState)
    }

    @Test
    fun `iniciar el timer de cardio delega al controller y su estado se refleja en cardioUiState`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val id = cardioExerciseId(routineRepository, SUNDAY_DAY_ID)
        val cardioTimer = FakeCardioTimer()
        val viewModel = viewModel(id, dayId = SUNDAY_DAY_ID, routineRepository = routineRepository, cardioTimer = cardioTimer)
        viewModel.uiState.first { !it.isLoading }

        viewModel.startCardioTimer()
        cardioTimer.advanceTo(42)

        val state = viewModel.uiState.first()
        assertEquals(CardioTimerStatus.RUNNING, state.cardioUiState?.timerState)
        assertEquals(42, state.cardioUiState?.elapsedSeconds)
        assertFalse(state.cardioUiState!!.canComplete)
    }

    @Test
    fun `completeCardioSession no hace nada si el timer sigue corriendo`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val id = cardioExerciseId(routineRepository, SUNDAY_DAY_ID)
        val cardioTimer = FakeCardioTimer()
        val cardioSessionRepository = FakeCardioSessionRepository()
        val viewModel = viewModel(
            id, dayId = SUNDAY_DAY_ID, routineRepository = routineRepository,
            cardioTimer = cardioTimer, cardioSessionRepository = cardioSessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }
        viewModel.startCardioTimer()
        cardioTimer.advanceTo(30)

        viewModel.completeCardioSession()

        assertTrue(cardioSessionRepository.observeSessions(ExerciseCatalog.caminarEnCinta.id).first().isEmpty())
    }

    @Test
    fun `completeCardioSession no hace nada si no transcurrio tiempo`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val id = cardioExerciseId(routineRepository, SUNDAY_DAY_ID)
        val cardioTimer = FakeCardioTimer()
        val cardioSessionRepository = FakeCardioSessionRepository()
        val viewModel = viewModel(
            id, dayId = SUNDAY_DAY_ID, routineRepository = routineRepository,
            cardioTimer = cardioTimer, cardioSessionRepository = cardioSessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }
        viewModel.startCardioTimer()
        viewModel.stopCardioTimer()

        viewModel.completeCardioSession()

        assertTrue(cardioSessionRepository.observeSessions(ExerciseCatalog.caminarEnCinta.id).first().isEmpty())
    }

    @Test
    fun `completeCardioSession guarda la sesion con los datos ingresados y avanza al siguiente ejercicio`() = runTest {
        val routineRepository = FakeRoutineRepository()
        val id = cardioExerciseId(routineRepository, SUNDAY_DAY_ID)
        val cardioTimer = FakeCardioTimer()
        val cardioSessionRepository = FakeCardioSessionRepository()
        val viewModel = viewModel(
            id, dayId = SUNDAY_DAY_ID, routineRepository = routineRepository,
            cardioTimer = cardioTimer, cardioSessionRepository = cardioSessionRepository,
        )
        viewModel.uiState.first { !it.isLoading }

        viewModel.startCardioTimer()
        cardioTimer.advanceTo(600)
        viewModel.onDistanceInputChanged("5.2")
        viewModel.onHeartRateInputChanged("140")
        viewModel.onCaloriesInputChanged("300")
        viewModel.stopCardioTimer()
        viewModel.completeCardioSession()

        val sessions = cardioSessionRepository.observeSessions(ExerciseCatalog.caminarEnCinta.id).first()
        assertEquals(1, sessions.size)
        val session = sessions.single()
        assertEquals(600, session.durationSeconds)
        assertEquals(5.2, session.distanceKm)
        assertEquals(140, session.avgHeartRate)
        assertEquals(300, session.calories)

        // Se resetea el formulario para poder registrar una sesión nueva de este mismo ejercicio.
        val state = viewModel.uiState.first()
        assertEquals(0, state.cardioUiState?.elapsedSeconds)
        assertEquals("", state.cardioUiState?.distanceKmInput)
    }
}
