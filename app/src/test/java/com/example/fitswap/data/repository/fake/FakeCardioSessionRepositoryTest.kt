package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.CardioSession
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val EXERCISE_ID = "caminar-en-cinta"

private fun session(
    id: String,
    durationSeconds: Int = 600,
    distanceKm: Double? = 5.0,
) = CardioSession(
    id = id, exerciseId = EXERCISE_ID, date = LocalDate.of(2026, 9, 1),
    durationSeconds = durationSeconds, distanceKm = distanceKm, avgHeartRate = 140, calories = 300,
)

class FakeCardioSessionRepositoryTest {

    @Test
    fun `agregar dos sesiones con ids distintos las conserva a ambas`() = runTest {
        val repository = FakeCardioSessionRepository()

        repository.addSession(session("s1"))
        repository.addSession(session("s2"))

        val sessions = repository.observeSessions(EXERCISE_ID).first()
        assertEquals(2, sessions.size)
    }

    @Test
    fun `agregar una sesion con un id ya existente la reemplaza en vez de duplicarla`() = runTest {
        val repository = FakeCardioSessionRepository()
        repository.addSession(session("s1", durationSeconds = 600))

        repository.addSession(session("s1", durationSeconds = 900))

        val sessions = repository.observeSessions(EXERCISE_ID).first()
        assertEquals(1, sessions.size)
        assertEquals(900, sessions.single().durationSeconds)
    }

    @Test
    fun `updateSession reemplaza los datos de una sesion existente por su id`() = runTest {
        val repository = FakeCardioSessionRepository()
        repository.addSession(session("s1", distanceKm = 5.0))

        repository.updateSession(session("s1", distanceKm = 7.5))

        val sessions = repository.observeSessions(EXERCISE_ID).first()
        assertEquals(1, sessions.size)
        assertEquals(7.5, sessions.single().distanceKm)
    }

    @Test
    fun `deleteSession la quita sin afectar otras sesiones`() = runTest {
        val repository = FakeCardioSessionRepository()
        repository.addSession(session("s1"))
        repository.addSession(session("s2"))

        repository.deleteSession("s1")

        val sessions = repository.observeSessions(EXERCISE_ID).first()
        assertTrue(sessions.none { it.id == "s1" })
        assertEquals(1, sessions.size)
    }
}
