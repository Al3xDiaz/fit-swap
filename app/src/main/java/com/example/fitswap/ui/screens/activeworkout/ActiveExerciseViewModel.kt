package com.example.fitswap.ui.screens.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitswap.data.repository.CardioSessionRepository
import com.example.fitswap.data.repository.GalleryRepository
import com.example.fitswap.data.repository.HistoryRepository
import com.example.fitswap.data.repository.NotesRepository
import com.example.fitswap.data.repository.RoutineRepository
import com.example.fitswap.data.repository.SetRepository
import com.example.fitswap.data.repository.SettingsRepository
import com.example.fitswap.data.repository.SubstituteRepository
import com.example.fitswap.data.repository.WorkoutSessionRepository
import com.example.fitswap.data.time.CurrentDateProvider
import com.example.fitswap.domain.logic.ExerciseHistoryAnalytics
import com.example.fitswap.domain.logic.HistorySession
import com.example.fitswap.domain.logic.SetPlanner
import com.example.fitswap.domain.model.CardioSession
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.ExerciseType
import com.example.fitswap.domain.model.GalleryItem
import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.PlannedSet
import com.example.fitswap.domain.model.RoutineExercise
import com.example.fitswap.domain.model.SetType
import com.example.fitswap.timer.CardioTimer
import com.example.fitswap.timer.CardioTimerStatus
import com.example.fitswap.timer.RestTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseHistorySummary(
    val exerciseId: String,
    val exerciseName: String,
    val maxWeightKg: Double?,
    val totalVolumeKg: Double,
    val recentSessions: List<HistorySession>,
    val notesByDate: Map<LocalDate, String> = emptyMap(),
)

/** Estado del timer/formulario de cardio — solo relevante cuando [ActiveExerciseUiState.exerciseType]
 * es [ExerciseType.CARDIO] (ver `CardioTab`). Distancia/ritmo cardíaco/calorías son de entrada
 * manual; la duración la da el propio timer. */
data class CardioUiState(
    val timerState: CardioTimerStatus = CardioTimerStatus.STOPPED,
    val elapsedSeconds: Int = 0,
    val distanceKmInput: String = "",
    val avgHeartRateInput: String = "",
    val caloriesInput: String = "",
) {
    /** Se puede registrar la sesión una vez que el timer no está corriendo y ya transcurrió algo
     * de tiempo — mientras está pausado también se puede, sin necesidad de detenerlo antes. */
    val canComplete: Boolean get() = timerState != CardioTimerStatus.RUNNING && elapsedSeconds > 0
}

data class ActiveExerciseUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null,
    val exerciseType: ExerciseType = ExerciseType.STRENGTH,
    val cardioUiState: CardioUiState? = null,
    val cardioSessions: List<CardioSession> = emptyList(),
    val phases: List<PhaseStatus> = emptyList(),
    val effectiveWeightKg: Double = 1.0,
    /** Texto tal cual lo tipea el usuario en el campo de peso efectivo — puede estar vacío
     * mientras edita; [effectiveWeightKg] es el último valor válido comprometido. */
    val effectiveWeightInput: String = "1.0",
    val lastLoggedWeightKg: Double? = null,
    val currentSet: PlannedSet? = null,
    val currentStageWeightKg: Double = 1.0,
    /** Texto tal cual lo tipea el usuario en el campo de peso de la etapa actual — mismo
     * criterio que [effectiveWeightInput]. */
    val currentStageWeightInput: String = "1.0",
    val currentStageWeightIsEffective: Boolean = false,
    val currentReps: Int = 0,
    /** Texto tal cual lo tipea el usuario en el campo de reps — mismo criterio que
     * [effectiveWeightInput]. */
    val currentRepsInput: String = "0",
    val effectiveSetsDone: Int = 0,
    val effectiveSetsTotal: Int = 0,
    val restRemainingSeconds: Int? = null,
    val restTotalSeconds: Int = 0,
    val isComplete: Boolean = false,
    /** Cuenta regresiva (3, 2, 1) que arranca sola al completar la última serie planificada;
     * al llegar a 0 (o si se toca antes el botón "Completar ejercicio") se guardan en bloque
     * todas las series de la sesión — hasta entonces no se persistió nada (ver [registerSet]). */
    val completionCountdownSeconds: Int? = null,
    /** Id del ejercicio al que hay que avanzar automáticamente apenas termina de guardarse la
     * sesión completa — la pantalla lo consume una sola vez y navega (ver [consumeAutoAdvance]). */
    val autoAdvanceToExerciseId: String? = null,
    /** Ids del ejercicio anterior/siguiente del día para los botones de navegación manual — el
     * timer ya no avanza de ejercicio solo (ver [startRestTimer]). */
    val previousExerciseId: String? = null,
    val nextExerciseId: String? = null,
    val galleryItems: List<GalleryItem> = emptyList(),
    val historySummaries: List<ExerciseHistorySummary> = emptyList(),
)

private const val COMPLETION_COUNTDOWN_SECONDS = 3

data class PhaseStatus(val type: SetType, val state: PhaseState)

enum class PhaseState { DONE, ACTIVE, PENDING }

@HiltViewModel
class ActiveExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val setRepository: SetRepository,
    private val historyRepository: HistoryRepository,
    private val substituteRepository: SubstituteRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val currentDateProvider: CurrentDateProvider,
    private val settingsRepository: SettingsRepository,
    private val galleryRepository: GalleryRepository,
    private val notesRepository: NotesRepository,
    private val restTimerController: RestTimer,
    private val cardioSessionRepository: CardioSessionRepository,
    private val cardioTimerController: CardioTimer,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle["routineId"])
    private val dayId: String = checkNotNull(savedStateHandle["dayId"])
    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow(ActiveExerciseUiState())
    val uiState: StateFlow<ActiveExerciseUiState> = _uiState.asStateFlow()

    private var routineExercise: RoutineExercise? = null
    private var plan: List<PlannedSet> = emptyList()
    private var planIndex = 0
    private var loggedSetSeq = 0
    private var effectiveWeightKg = 1.0
    private var manualStageWeightOverrideKg: Double? = null
    private var substitutedExercise: Exercise? = null
    private var historyJob: Job? = null
    private var galleryJob: Job? = null
    private var completionCountdownJob: Job? = null

    /** Ids de las series/puntos de historial persistidos en esta sesión de pantalla (ver
     * [registerSet], que ya los guarda en Room de inmediato — no se espera a completar el
     * ejercicio, para no perderlos si el proceso muere en background, p. ej. al cancelar el timer
     * de descanso desde su notificación). Ya no se usan para saber qué borrar al salir — eso ahora
     * se hace por fecha (ver `RoutineSessionFinisher`/`discardExerciseData`) —, solo para saber si
     * hay algo pendiente de esta pantalla que auto-completar (ver [completeExercise] y
     * [refreshUiState]'s `hasPendingProgress`). */
    private val sessionLoggedSetIds = mutableListOf<String>()
    private val sessionHistoryPointIds = mutableListOf<String>()

    /** Id (slot) del ejercicio anterior/siguiente del día, o nulo si no hay — mismo orden
     * (`RoutineDay.exercises`) que ya usa `SessionMenuViewModel`. Alimentan los botones
     * "Anterior"/"Siguiente", que están siempre disponibles (la navegación entre ejercicios es
     * manual, no depende de si el actual está completo). */
    private var nextRoutineExerciseId: String? = null
    private var previousRoutineExerciseId: String? = null

    init {
        viewModelScope.launch {
            restTimerController.remainingSeconds.collect { remaining ->
                _uiState.update { it.copy(restRemainingSeconds = remaining) }
            }
        }

        viewModelScope.launch {
            cardioTimerController.state.collect { timerState ->
                _uiState.update {
                    it.copy(
                        cardioUiState = (it.cardioUiState ?: CardioUiState()).copy(
                            timerState = timerState.status,
                            elapsedSeconds = timerState.elapsedSeconds,
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            val routine = routineRepository.observeRoutine(routineId).first { it != null } ?: return@launch
            val day = routine.days.firstOrNull { it.id == dayId } ?: return@launch
            val exercise = day.exercises.firstOrNull { it.id == exerciseId } ?: return@launch

            routineExercise = exercise
            val exerciseIndex = day.exercises.indexOf(exercise)
            nextRoutineExerciseId = day.exercises.getOrNull(exerciseIndex + 1)?.id
            previousRoutineExerciseId = day.exercises.getOrNull(exerciseIndex - 1)?.id

            // El plan de series (warmup/approach/effective) solo aplica a peso — un ejercicio de
            // cardio se resuelve enteramente dentro del `collect` de abajo, sin `SetPlanner`.
            if (exercise.exercise.type == ExerciseType.STRENGTH) {
                val restTimerSeconds = settingsRepository.observeSettings().first().restTimerSeconds
                plan = SetPlanner.buildPlan(exercise, restTimerSeconds)

                val existingLogged = setRepository.observeLoggedSets(exercise.id, currentDateProvider.today()).first()
                planIndex = existingLogged.size.coerceAtMost(plan.size)
                loggedSetSeq = existingLogged.size
            }

            workoutSessionRepository.observeSubstitution(exercise.id).collect { substitution ->
                substitutedExercise = substitution
                val activeExercise = substitution ?: exercise.exercise
                _uiState.update { it.copy(exerciseType = activeExercise.type) }

                if (activeExercise.type == ExerciseType.CARDIO) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            exercise = activeExercise,
                            previousExerciseId = previousRoutineExerciseId,
                            nextExerciseId = nextRoutineExerciseId,
                            cardioUiState = it.cardioUiState ?: CardioUiState(),
                        )
                    }
                    loadCardioSessions(activeExercise)

                    galleryJob?.cancel()
                    galleryJob = viewModelScope.launch {
                        galleryRepository.observeGallery(activeExercise.id).collect { items ->
                            _uiState.update { it.copy(galleryItems = items) }
                        }
                    }
                    return@collect
                }

                val lastWeight = resolveSuggestedWeight(activeExercise)
                effectiveWeightKg = SetPlanner.suggestedEffectiveWeight(lastWeight)
                manualStageWeightOverrideKg = null
                _uiState.update {
                    it.copy(
                        lastLoggedWeightKg = lastWeight,
                        previousExerciseId = previousRoutineExerciseId,
                        nextExerciseId = nextRoutineExerciseId,
                    )
                }
                refreshUiState()
                loadHistorySummaries(activeExercise)

                galleryJob?.cancel()
                galleryJob = viewModelScope.launch {
                    galleryRepository.observeGallery(activeExercise.id).collect { items ->
                        _uiState.update { it.copy(galleryItems = items) }
                    }
                }
            }
        }
    }

    /** Peso efectivo sugerido: el último registrado para el ejercicio, o para alguno de sus
     * sustitutos si no hay historial propio (ver `docs/DESIGN_DOC.md#modelo-de-datos-borrador`). */
    private suspend fun resolveSuggestedWeight(exercise: Exercise): Double? {
        historyRepository.lastWeightKg(exercise.id)?.let { return it }
        val substitutes = substituteRepository.substitutesFor(exercise.id)
        return substitutes.firstNotNullOfOrNull { historyRepository.lastWeightKg(it.id) }
    }

    /** Historial del ejercicio activo y de cada uno de sus sustitutos, por separado — cada uno
     * mantiene su propio peso/progreso independiente (tab "Historial"). */
    private fun loadHistorySummaries(activeExercise: Exercise) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            val substitutes = substituteRepository.substitutesFor(activeExercise.id)
            val exercises = listOf(activeExercise) + substitutes
            val historyByExercise = combine(exercises.map { historyRepository.observeHistory(it.id) }) { it }
            val notesByExerciseFlow = combine(exercises.map { notesRepository.observeNotes(it.id) }) { it }
            combine(historyByExercise, notesByExerciseFlow) { pointsByExercise, notesByExercise ->
                exercises.mapIndexed { index, ex ->
                    val points = pointsByExercise[index]
                    val notes = notesByExercise[index]
                    ExerciseHistorySummary(
                        exerciseId = ex.id,
                        exerciseName = ex.name,
                        maxWeightKg = ExerciseHistoryAnalytics.maxEffectiveWeightKg(points),
                        totalVolumeKg = ExerciseHistoryAnalytics.totalEffectiveVolumeKg(points),
                        recentSessions = ExerciseHistoryAnalytics.recentSessions(points),
                        notesByDate = notes.associate { note -> note.date to note.text },
                    )
                }
            }.collect { summaries -> _uiState.update { it.copy(historySummaries = summaries) } }
        }
    }

    /** Historial de sesiones de cardio del ejercicio activo — sin analítica agregada, solo la
     * lista cruda (ver alcance excluido en el plan: sin promedios/tendencias). Reutiliza
     * [historyJob] porque, para un mismo ViewModel, solo uno de los dos flujos (peso o cardio)
     * está activo a la vez según [ActiveExerciseUiState.exerciseType]. */
    private fun loadCardioSessions(activeExercise: Exercise) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            cardioSessionRepository.observeSessions(activeExercise.id).collect { sessions ->
                _uiState.update { it.copy(cardioSessions = sessions) }
            }
        }
    }

    fun startCardioTimer() = cardioTimerController.start()
    fun pauseCardioTimer() = cardioTimerController.pause()
    fun resumeCardioTimer() = cardioTimerController.resume()
    fun stopCardioTimer() = cardioTimerController.stop()

    fun onDistanceInputChanged(text: String) {
        _uiState.update { it.copy(cardioUiState = (it.cardioUiState ?: CardioUiState()).copy(distanceKmInput = text)) }
    }

    fun onHeartRateInputChanged(text: String) {
        _uiState.update {
            it.copy(cardioUiState = (it.cardioUiState ?: CardioUiState()).copy(avgHeartRateInput = text))
        }
    }

    fun onCaloriesInputChanged(text: String) {
        _uiState.update { it.copy(cardioUiState = (it.cardioUiState ?: CardioUiState()).copy(caloriesInput = text)) }
    }

    /** Guarda la sesión de cardio completa (duración la da el timer; distancia/ritmo/calorías
     * quedan `null` si el usuario no las completó) y avanza al siguiente ejercicio, igual que
     * [completeExercise] para el flujo de peso. Detiene el timer solo si seguía corriendo/pausado
     * — llamar `stop()` sobre un timer ya detenido reinicia el foreground service sin que este
     * llegue a llamar `startForeground()`, lo cual crashea. */
    fun completeCardioSession() {
        val exercise = routineExercise ?: return
        val cardio = _uiState.value.cardioUiState ?: return
        if (!cardio.canComplete) return
        val displayExercise = substitutedExercise ?: exercise.exercise
        if (cardio.timerState != CardioTimerStatus.STOPPED) cardioTimerController.stop()

        viewModelScope.launch {
            cardioSessionRepository.addSession(
                CardioSession(
                    id = "${displayExercise.id}-${loggedSetSeq++}",
                    exerciseId = displayExercise.id,
                    date = currentDateProvider.today(),
                    durationSeconds = cardio.elapsedSeconds,
                    distanceKm = cardio.distanceKmInput.toDoubleOrNull(),
                    avgHeartRate = cardio.avgHeartRateInput.toIntOrNull(),
                    calories = cardio.caloriesInput.toIntOrNull(),
                )
            )
            _uiState.update { it.copy(cardioUiState = CardioUiState(), autoAdvanceToExerciseId = nextRoutineExerciseId) }
        }
    }

    /** Edición del campo de peso efectivo: el texto se refleja tal cual (permite quedar
     * vacío mientras se edita); el valor comprometido ([effectiveWeightKg], usado por la
     * lógica de plan/registro) solo se actualiza cuando el texto parsea a un número válido. */
    fun onEffectiveWeightInputChanged(text: String) {
        _uiState.update { it.copy(effectiveWeightInput = text) }
        text.toDoubleOrNull()?.let { parsed ->
            effectiveWeightKg = parsed.coerceAtLeast(0.0)
            recomputeStageWeight()
        }
    }

    /** Si el campo de peso efectivo queda vacío al perder el foco, se restaura a 1. */
    fun onEffectiveWeightFocusChanged(isFocused: Boolean) {
        if (isFocused || _uiState.value.effectiveWeightInput.isNotBlank()) return
        effectiveWeightKg = 1.0
        _uiState.update { it.copy(effectiveWeightInput = formatWeight(1.0)) }
        recomputeStageWeight()
    }

    /** Mismo criterio que [onEffectiveWeightInputChanged] para el peso sugerido de la etapa
     * actual (cuando la etapa no es efectiva) o, si lo es, delega en el peso efectivo. */
    fun onStageWeightInputChanged(text: String) {
        _uiState.update { it.copy(currentStageWeightInput = text) }
        text.toDoubleOrNull()?.let { parsed ->
            if (_uiState.value.currentStageWeightIsEffective) {
                effectiveWeightKg = parsed.coerceAtLeast(0.0)
            } else {
                manualStageWeightOverrideKg = parsed.coerceAtLeast(0.0)
            }
            recomputeStageWeight()
        }
    }

    /** Si el campo de peso de etapa queda vacío al perder el foco, se restaura a 1. */
    fun onStageWeightFocusChanged(isFocused: Boolean) {
        if (isFocused || _uiState.value.currentStageWeightInput.isNotBlank()) return
        if (_uiState.value.currentStageWeightIsEffective) {
            effectiveWeightKg = 1.0
        } else {
            manualStageWeightOverrideKg = 1.0
        }
        _uiState.update { it.copy(currentStageWeightInput = formatWeight(1.0)) }
        recomputeStageWeight()
    }

    fun incrementReps() {
        _uiState.update { it.copy(currentReps = it.currentReps + 1, currentRepsInput = "${it.currentReps + 1}") }
    }

    fun decrementReps() {
        _uiState.update {
            val newReps = (it.currentReps - 1).coerceAtLeast(0)
            it.copy(currentReps = newReps, currentRepsInput = "$newReps")
        }
    }

    /** Edición directa por teclado del campo de reps, además de los botones +/-. Mismo
     * criterio que [onEffectiveWeightInputChanged]: el texto se refleja tal cual, el valor
     * comprometido solo se actualiza cuando parsea. */
    fun onRepsInputChanged(text: String) {
        _uiState.update { it.copy(currentRepsInput = text) }
        text.toIntOrNull()?.let { parsed ->
            _uiState.update { it.copy(currentReps = parsed.coerceAtLeast(0)) }
        }
    }

    /** Si el campo de reps queda vacío al perder el foco, se restaura a 1. */
    fun onRepsFocusChanged(isFocused: Boolean) {
        if (isFocused || _uiState.value.currentRepsInput.isNotBlank()) return
        _uiState.update { it.copy(currentReps = 1, currentRepsInput = "1") }
    }

    /** Recalcula solo el peso de etapa derivado del peso efectivo/override manual, sin tocar
     * fase, reps ni completitud — se usa mientras el usuario edita los campos de peso, para no
     * pisar lo que está tipeando en otros campos (ver [refreshUiState] para el recálculo
     * completo que sí corresponde al avanzar de etapa). */
    private fun recomputeStageWeight() {
        val current = plan.getOrNull(planIndex)
        val stageWeightKg = when {
            current == null -> effectiveWeightKg
            current.type == SetType.EFFECTIVE -> effectiveWeightKg
            else -> manualStageWeightOverrideKg ?: (current.weightFactor * effectiveWeightKg)
        }
        _uiState.update { it.copy(effectiveWeightKg = effectiveWeightKg, currentStageWeightKg = stageWeightKg) }
    }

    fun registerSet() {
        val exercise = routineExercise ?: return
        val current = plan.getOrNull(planIndex) ?: return
        val state = _uiState.value
        val displayExercise = substitutedExercise ?: exercise.exercise
        val seq = loggedSetSeq++

        val loggedSet = LoggedSet(
            id = "${exercise.id}-$seq",
            routineExerciseId = exercise.id,
            type = current.type,
            reps = state.currentReps,
            weightKg = state.currentStageWeightKg,
            date = currentDateProvider.today(),
        )
        val historyPoint = HistoryPoint(
            id = "${displayExercise.id}-$seq",
            exerciseId = displayExercise.id,
            date = currentDateProvider.today(),
            type = current.type,
            reps = state.currentReps,
            weightKg = state.currentStageWeightKg,
        )
        sessionLoggedSetIds += loggedSet.id
        sessionHistoryPointIds += historyPoint.id

        // Se persiste de inmediato (no se espera a "Completar ejercicio") para no perder la
        // serie si el proceso muere en background — ver comentario de [sessionLoggedSetIds].
        viewModelScope.launch {
            setRepository.logSet(loggedSet)
            historyRepository.addHistoryPoint(historyPoint)
        }

        planIndex++
        manualStageWeightOverrideKg = null
        startRestTimer(current.restSeconds)
        refreshUiState()
    }

    /** Cuenta regresiva que arranca sola apenas [refreshUiState] detecta que no quedan series
     * planificadas — da tiempo a tocar "Completar ejercicio" antes de que dispare sola. */
    private fun startCompletionCountdown() {
        completionCountdownJob?.cancel()
        completionCountdownJob = viewModelScope.launch {
            for (remaining in COMPLETION_COUNTDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(completionCountdownSeconds = remaining) }
                delay(1_000)
            }
            completeExercise()
        }
    }

    /** Marca el ejercicio como completo y a qué ejercicio avanzar — las series ya se persistieron
     * una a una en [registerSet], acá solo se cierra la sesión. La pantalla consume
     * [ActiveExerciseUiState.autoAdvanceToExerciseId] una sola vez y navega (ver
     * [consumeAutoAdvance]). Se puede llamar tanto desde el botón "Completar ejercicio" como desde
     * el propio countdown al llegar a 0; es seguro llamarla dos veces (la segunda no encuentra
     * nada pendiente de esta sesión). */
    fun completeExercise() {
        completionCountdownJob?.cancel()
        completionCountdownJob = null
        if (sessionLoggedSetIds.isEmpty() && sessionHistoryPointIds.isEmpty()) return

        sessionLoggedSetIds.clear()
        sessionHistoryPointIds.clear()
        _uiState.update {
            it.copy(completionCountdownSeconds = null, autoAdvanceToExerciseId = nextRoutineExerciseId)
        }
    }

    /** La pantalla la llama apenas consume [ActiveExerciseUiState.autoAdvanceToExerciseId] para
     * navegar — evita navegar de nuevo en la siguiente recomposición. */
    fun consumeAutoAdvance() {
        _uiState.update { it.copy(autoAdvanceToExerciseId = null) }
    }

    /** El descanso corre en [RestTimerController] (respaldado por un foreground service) para
     * sobrevivir a que la app se minimice y para poder notificar con sonido al terminar. Ya no
     * avanza de ejercicio solo al llegar a cero — el usuario navega manualmente con los botones
     * "Anterior"/"Siguiente" (ver [ActiveExerciseUiState.previousExerciseId]/[nextExerciseId]). */
    private fun startRestTimer(totalSeconds: Int) {
        _uiState.update { it.copy(restTotalSeconds = totalSeconds) }
        restTimerController.start(totalSeconds)
    }

    /** Termina la rutina al confirmar la salida (back): borra explícitamente las series/puntos de
     * historial persistidos en esta sesión para el ejercicio actual todavía no completado (ver
     * [registerSet]/[completeExercise]) — cumple el mensaje del diálogo de salida ("se descartará
     * el progreso de este ejercicio") — y limpia las sustituciones de la sesión, igual que
     * `SessionMenuViewModel.endRoutine()`. */
    fun endRoutine() {
        viewModelScope.launch {
            if (sessionLoggedSetIds.isNotEmpty()) setRepository.deleteSets(sessionLoggedSetIds)
            if (sessionHistoryPointIds.isNotEmpty()) historyRepository.deleteHistoryPoints(sessionHistoryPointIds)
            sessionLoggedSetIds.clear()
            sessionHistoryPointIds.clear()
            workoutSessionRepository.clearAll()
        }
    }

    /** "Terminar y guardar" desde el último ejercicio del día ([ActiveExerciseUiState.nextExerciseId]
     * nulo): a diferencia de [endRoutine], **conserva** todo lo ya persistido de esta sesión —
     * incluso el progreso a medias del ejercicio en curso — en vez de descartarlo. Solo vacía el
     * registro en memoria de la sesión (ya no hace falta borrarlo al salir) y limpia las
     * sustituciones, igual que `SessionMenuViewModel.endRoutine()`. */
    fun finishRoutineKeepingProgress() {
        completionCountdownJob?.cancel()
        completionCountdownJob = null
        sessionLoggedSetIds.clear()
        sessionHistoryPointIds.clear()
        viewModelScope.launch { workoutSessionRepository.clearAll() }
    }

    private fun refreshUiState() {
        val exercise = routineExercise ?: return
        val displayExercise = substitutedExercise ?: exercise.exercise
        val current = plan.getOrNull(planIndex)
        val stageWeightKg = when {
            current == null -> effectiveWeightKg
            current.type == SetType.EFFECTIVE -> effectiveWeightKg
            else -> manualStageWeightOverrideKg ?: (current.weightFactor * effectiveWeightKg)
        }
        val effectiveSetsDone = plan.take(planIndex).count { it.type == SetType.EFFECTIVE }
        val effectiveSetsTotal = plan.count { it.type == SetType.EFFECTIVE }

        val newReps = current?.plannedReps ?: _uiState.value.currentReps

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                exercise = displayExercise,
                phases = buildPhaseStatuses(current?.type),
                effectiveWeightKg = effectiveWeightKg,
                effectiveWeightInput = formatWeight(effectiveWeightKg),
                currentSet = current,
                currentStageWeightKg = stageWeightKg,
                currentStageWeightInput = formatWeight(stageWeightKg),
                currentStageWeightIsEffective = current?.type == SetType.EFFECTIVE,
                currentReps = newReps,
                currentRepsInput = "$newReps",
                effectiveSetsDone = effectiveSetsDone,
                effectiveSetsTotal = effectiveSetsTotal,
                isComplete = current == null,
            )
        }

        val hasPendingProgress = sessionLoggedSetIds.isNotEmpty() || sessionHistoryPointIds.isNotEmpty()
        if (current == null && hasPendingProgress) {
            // Solo arranca la cuenta regresiva si hay algo pendiente de esta sesión — si se
            // reabre un ejercicio ya completado antes (nada pendiente), se queda en el mensaje
            // estático de completado sin disparar un guardado/avance vacío.
            startCompletionCountdown()
        } else {
            completionCountdownJob?.cancel()
            completionCountdownJob = null
            _uiState.update { it.copy(completionCountdownSeconds = null) }
        }
    }

    private fun formatWeight(kg: Double): String = "%.1f".format(kg)

    private fun buildPhaseStatuses(currentType: SetType?): List<PhaseStatus> =
        SetType.entries.map { type ->
            val itemsOfType = plan.filter { it.type == type }
            val lastIndexOfType = plan.indexOfLast { it.type == type }
            val state = when {
                type == currentType -> PhaseState.ACTIVE
                itemsOfType.isEmpty() || lastIndexOfType < planIndex -> PhaseState.DONE
                else -> PhaseState.PENDING
            }
            PhaseStatus(type, state)
        }
}
