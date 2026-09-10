package com.example.fitswap.data.repository.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.dao.SetDao
import com.example.fitswap.data.local.entity.ExerciseEntity
import com.example.fitswap.data.local.entity.LoggedSetEntity
import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import com.example.fitswap.domain.model.LoggedSet
import com.example.fitswap.domain.model.SetType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private val TODAY = LocalDate.of(2026, 9, 9)
private val YESTERDAY = LocalDate.of(2026, 9, 8)

@RunWith(AndroidJUnit4::class)
class RoomSetRepositoryTest {

    private lateinit var database: FitSwapDatabase
    private lateinit var repository: RoomSetRepository
    private lateinit var setDao: SetDao

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
        setDao = database.setDao()
        repository = RoomSetRepository(setDao)

        database.exerciseDao().insert(
            ExerciseEntity(id = "press-de-pecho", name = "Press de pecho", muscleGroup = "Pecho", equipment = "Máquina", tags = emptyList())
        )
        database.routineDao().insertRoutine(RoutineEntity(id = "r1", name = "Rutina de prueba", isDefault = false))
        database.routineDao().insertDay(RoutineDayEntity(id = "d1", routineId = "r1", name = "Día 1", dayOfWeek = null, orderIndex = 0))
        database.routineDao().insertRoutineExercise(
            RoutineExerciseEntity(
                id = "re1", dayId = "d1", exerciseId = "press-de-pecho", approachSets = 1, effectiveSets = 3,
                effectiveRepsLabel = "8-12", approachGuideline = null, restLabel = "90 s", usesStraps = false,
                notes = null, orderIndex = 0,
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun loggedSet(id: String, date: LocalDate) = LoggedSet(
        id = id, routineExerciseId = "re1", type = SetType.EFFECTIVE, reps = 10, weightKg = 50.0, date = date,
    )

    @Test
    fun observeLoggedSetsSoloDevuelveLasDeEsaFecha() = runBlocking {
        repository.logSet(loggedSet("s-hoy", TODAY))
        repository.logSet(loggedSet("s-ayer", YESTERDAY))

        val hoy = repository.observeLoggedSets("re1", TODAY).first()

        assertEquals(1, hoy.size)
        assertEquals("s-hoy", hoy.single().id)
    }

    @Test
    fun unEjercicioCompletadoAyerNoApareceCompletoHoy() = runBlocking {
        // Regresión del bug: completar un ejercicio ayer no debe dejarlo "completo" hoy.
        repeat(4) { index -> repository.logSet(loggedSet("s-ayer-$index", YESTERDAY)) }

        val hoy = repository.observeLoggedSets("re1", TODAY).first()

        assertTrue("las series de ayer no deberían contar para hoy", hoy.isEmpty())
    }

    @Test
    fun deleteLoggedSetsForDateSoloBorraEseEjercicioEnEsaFecha() = runBlocking {
        repository.logSet(loggedSet("s-hoy", TODAY))
        repository.logSet(loggedSet("s-ayer", YESTERDAY))
        database.routineDao().insertRoutineExercise(
            RoutineExerciseEntity(
                id = "re2", dayId = "d1", exerciseId = "press-de-pecho", approachSets = 0, effectiveSets = 3,
                effectiveRepsLabel = "8-12", approachGuideline = null, restLabel = "90 s", usesStraps = false,
                notes = null, orderIndex = 1,
            )
        )
        setDao.insert(
            LoggedSetEntity(id = "s-otro-ejercicio", routineExerciseId = "re2", type = SetType.EFFECTIVE, reps = 10, weightKg = 50.0, date = TODAY)
        )

        repository.deleteLoggedSetsForDate("re1", TODAY)

        assertTrue(repository.observeLoggedSets("re1", TODAY).first().isEmpty())
        assertEquals(1, repository.observeLoggedSets("re1", YESTERDAY).first().size) // no afecta otra fecha
        assertEquals(1, repository.observeLoggedSets("re2", TODAY).first().size) // no afecta otro ejercicio
    }

    @Test
    fun deleteAllLoggedSetsForDateBorraTodosLosEjerciciosDeEsaFecha() = runBlocking {
        database.routineDao().insertRoutineExercise(
            RoutineExerciseEntity(
                id = "re2", dayId = "d1", exerciseId = "press-de-pecho", approachSets = 0, effectiveSets = 3,
                effectiveRepsLabel = "8-12", approachGuideline = null, restLabel = "90 s", usesStraps = false,
                notes = null, orderIndex = 1,
            )
        )
        repository.logSet(loggedSet("s-re1-hoy", TODAY))
        repository.logSet(LoggedSet(id = "s-re2-hoy", routineExerciseId = "re2", type = SetType.EFFECTIVE, reps = 10, weightKg = 50.0, date = TODAY))
        repository.logSet(loggedSet("s-re1-ayer", YESTERDAY))

        repository.deleteAllLoggedSetsForDate(TODAY)

        assertTrue(repository.observeLoggedSets("re1", TODAY).first().isEmpty())
        assertTrue(repository.observeLoggedSets("re2", TODAY).first().isEmpty())
        assertEquals(1, repository.observeLoggedSets("re1", YESTERDAY).first().size) // no afecta otra fecha
    }
}
