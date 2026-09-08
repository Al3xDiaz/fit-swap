package com.example.fitswap.data.repository.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.domain.model.CardioSession
import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.ExerciseType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCardioSessionRepositoryTest {

    private lateinit var database: FitSwapDatabase
    private lateinit var cardioRepository: RoomCardioSessionRepository
    private lateinit var exerciseRepository: RoomExerciseRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
        cardioRepository = RoomCardioSessionRepository(database.cardioSessionDao())
        exerciseRepository = RoomExerciseRepository(
            database,
            database.exerciseDao(),
            database.galleryDao(),
            database.historyDao(),
            database.noteDao(),
            database.substituteDao(),
            database.cardioSessionDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun agregarUnaSesionDeCardioSePuedeObservarPorEjercicio() = runBlocking {
        val exercise = Exercise("caminar-en-cinta", "Caminar en cinta", "Cardio", "Cinta", type = ExerciseType.CARDIO)
        exerciseRepository.addExercise(exercise)

        cardioRepository.addSession(
            CardioSession(
                id = "session-1",
                exerciseId = exercise.id,
                date = LocalDate.of(2026, 9, 1),
                durationSeconds = 600,
                distanceKm = 5.2,
                avgHeartRate = 140,
                calories = 300,
            )
        )

        val sessions = cardioRepository.observeSessions(exercise.id).first()
        assertEquals(1, sessions.size)
        val session = sessions.single()
        assertEquals(600, session.durationSeconds)
        assertEquals(5.2, session.distanceKm)
        assertEquals(140, session.avgHeartRate)
        assertEquals(300, session.calories)
    }

    @Test
    fun updateSessionReemplazaLosDatosSinDuplicarLaFila() = runBlocking {
        val exercise = Exercise("caminar-en-cinta", "Caminar en cinta", "Cardio", "Cinta", type = ExerciseType.CARDIO)
        exerciseRepository.addExercise(exercise)
        cardioRepository.addSession(
            CardioSession("session-1", exercise.id, LocalDate.of(2026, 9, 1), 600, 5.2, 140, 300)
        )

        cardioRepository.updateSession(
            CardioSession("session-1", exercise.id, LocalDate.of(2026, 9, 1), 900, 7.5, 150, 450)
        )

        val sessions = cardioRepository.observeSessions(exercise.id).first()
        assertEquals(1, sessions.size)
        val session = sessions.single()
        assertEquals(900, session.durationSeconds)
        assertEquals(7.5, session.distanceKm)
    }

    @Test
    fun deleteSessionLaQuitaSinAfectarOtrasSesiones() = runBlocking {
        val exercise = Exercise("caminar-en-cinta", "Caminar en cinta", "Cardio", "Cinta", type = ExerciseType.CARDIO)
        exerciseRepository.addExercise(exercise)
        cardioRepository.addSession(CardioSession("session-1", exercise.id, LocalDate.of(2026, 9, 1), 600, 5.2, 140, 300))
        cardioRepository.addSession(CardioSession("session-2", exercise.id, LocalDate.of(2026, 9, 2), 600, 5.2, 140, 300))

        cardioRepository.deleteSession("session-1")

        val sessions = cardioRepository.observeSessions(exercise.id).first()
        assertEquals(1, sessions.size)
        assertEquals("session-2", sessions.single().id)
    }

    @Test
    fun borrarElEjercicioBorraEnCascadaSusSesionesDeCardio() = runBlocking {
        val exercise = Exercise("caminar-en-cinta", "Caminar en cinta", "Cardio", "Cinta", type = ExerciseType.CARDIO)
        exerciseRepository.addExercise(exercise)
        cardioRepository.addSession(
            CardioSession("session-1", exercise.id, LocalDate.of(2026, 9, 1), 600, 5.2, 140, 300)
        )

        exerciseRepository.deleteExercise(exercise.id)

        assertTrue(cardioRepository.observeSessions(exercise.id).first().isEmpty())
    }
}
