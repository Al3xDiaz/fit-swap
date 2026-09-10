package com.example.fitswap.data.session

import com.example.fitswap.data.repository.fake.ExerciseCatalog
import com.example.fitswap.data.repository.fake.FakeCardioSessionRepository
import com.example.fitswap.data.repository.fake.FakeHistoryRepository
import com.example.fitswap.data.repository.fake.FakeNotesRepository
import com.example.fitswap.data.repository.fake.FakeSetRepository
import com.example.fitswap.data.repository.fake.FakeWorkoutSessionRepository
import com.example.fitswap.data.time.FixedCurrentDateProvider
import com.example.fitswap.domain.model.CardioSession
import com.example.fitswap.domain.model.HistoryPoint
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.SetType
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val ROUTINE_EXERCISE_ID_A = "default-martes-elevaciones-laterales"
private const val ROUTINE_EXERCISE_ID_B = "default-martes-press-militar-maquina"

class RoutineSessionFinisherTest {

    private val currentDateProvider = FixedCurrentDateProvider(DayOfWeek.TUESDAY)
    private val today = currentDateProvider.today()
    private val yesterday = today.minusDays(1)

    private fun finisher(
        setRepository: FakeSetRepository = FakeSetRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
        cardioSessionRepository: FakeCardioSessionRepository = FakeCardioSessionRepository(),
        notesRepository: FakeNotesRepository = FakeNotesRepository(),
        workoutSessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
    ) = RoutineSessionFinisher(
        setRepository = setRepository,
        historyRepository = historyRepository,
        cardioSessionRepository = cardioSessionRepository,
        notesRepository = notesRepository,
        workoutSessionRepository = workoutSessionRepository,
        currentDateProvider = currentDateProvider,
    )

    @Test
    fun `saveAndFinish conserva todo lo registrado hoy y solo limpia sustituciones`() = runTest {
        val setRepository = FakeSetRepository()
        setRepository.logSet(
            LoggedSet(id = "s1", routineExerciseId = ROUTINE_EXERCISE_ID_A, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = today)
        )
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(ROUTINE_EXERCISE_ID_A, ExerciseCatalog.pressDeHombroEnMaquina)

        finisher(setRepository = setRepository, workoutSessionRepository = sessionRepository).saveAndFinish()

        assertEquals(1, setRepository.observeLoggedSets(ROUTINE_EXERCISE_ID_A, today).first().size)
        assertNull(sessionRepository.observeSubstitution(ROUTINE_EXERCISE_ID_A).first())
    }

    @Test
    fun `discardAndFinish borra las series de hoy de cualquier ejercicio, sin afectar otro dia`() = runTest {
        val setRepository = FakeSetRepository()
        setRepository.logSet(
            LoggedSet(id = "s-a-hoy", routineExerciseId = ROUTINE_EXERCISE_ID_A, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = today)
        )
        setRepository.logSet(
            LoggedSet(id = "s-b-hoy", routineExerciseId = ROUTINE_EXERCISE_ID_B, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = today)
        )
        setRepository.logSet(
            LoggedSet(id = "s-a-ayer", routineExerciseId = ROUTINE_EXERCISE_ID_A, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0, date = yesterday)
        )

        finisher(setRepository = setRepository).discardAndFinish()

        assertTrue(setRepository.observeLoggedSets(ROUTINE_EXERCISE_ID_A, today).first().isEmpty())
        assertTrue(setRepository.observeLoggedSets(ROUTINE_EXERCISE_ID_B, today).first().isEmpty())
        assertEquals(1, setRepository.observeLoggedSets(ROUTINE_EXERCISE_ID_A, yesterday).first().size)
    }

    @Test
    fun `discardAndFinish borra historial, sesiones de cardio y notas de hoy de cualquier ejercicio`() = runTest {
        val historyRepository = FakeHistoryRepository()
        historyRepository.addHistoryPoint(
            HistoryPoint(id = "h1", exerciseId = "elevaciones-laterales", date = today, type = SetType.EFFECTIVE, reps = 10, weightKg = 1.0)
        )
        val cardioSessionRepository = FakeCardioSessionRepository()
        cardioSessionRepository.addSession(
            CardioSession(
                id = "c1", exerciseId = ExerciseCatalog.caminarEnCinta.id, date = today,
                durationSeconds = 600, distanceKm = 5.0, avgHeartRate = 140, calories = 300,
            )
        )
        val notesRepository = FakeNotesRepository()
        notesRepository.setNote("elevaciones-laterales", today, "Nota de hoy")

        finisher(
            historyRepository = historyRepository,
            cardioSessionRepository = cardioSessionRepository,
            notesRepository = notesRepository,
        ).discardAndFinish()

        assertTrue(historyRepository.observeHistory("elevaciones-laterales").first().isEmpty())
        assertTrue(cardioSessionRepository.observeSessions(ExerciseCatalog.caminarEnCinta.id).first().isEmpty())
        assertNull(notesRepository.observeNote("elevaciones-laterales", today).first())
    }

    @Test
    fun `discardAndFinish tambien limpia las sustituciones de la sesion`() = runTest {
        val sessionRepository = FakeWorkoutSessionRepository()
        sessionRepository.substituteExercise(ROUTINE_EXERCISE_ID_A, ExerciseCatalog.pressDeHombroEnMaquina)

        finisher(workoutSessionRepository = sessionRepository).discardAndFinish()

        assertNull(sessionRepository.observeSubstitution(ROUTINE_EXERCISE_ID_A).first())
    }
}
