package com.example.fitswap.data.repository.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.entity.ExerciseEntity
import com.example.fitswap.data.repository.MoveDirection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRoutineRepositoryTest {

    private lateinit var database: FitSwapDatabase
    private lateinit var repository: RoomRoutineRepository

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
        repository = RoomRoutineRepository(database, database.routineDao(), database.exerciseDao())

        database.exerciseDao().insertAll(
            listOf(
                ExerciseEntity("press-de-pecho", "Press de pecho", "Pecho", "Máquina", emptyList()),
                ExerciseEntity("remo", "Remo", "Espalda", "Máquina", emptyList()),
                ExerciseEntity("curl", "Curl", "Bíceps", "Mancuerna", emptyList()),
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun freshRoutineWithDay(): Pair<String, String> {
        val routineId = repository.createRoutine("Rutina")
        repository.addDay(routineId, "Día único")
        val day = repository.observeRoutine(routineId).first()!!.days.single()
        return routineId to day.id
    }

    @Test
    fun agregarUnEjercicioDosVecesGeneraIdsUnicos() = runBlocking {
        val (routineId, dayId) = freshRoutineWithDay()

        repository.addExerciseToDay(routineId, dayId, "press-de-pecho")
        repository.addExerciseToDay(routineId, dayId, "press-de-pecho")

        val exercises = repository.observeRoutine(routineId).first()!!.days.single().exercises
        assertEquals(2, exercises.size)
        assertEquals(exercises.map { it.id }.toSet().size, exercises.size)
    }

    @Test
    fun moverUnEjercicioHaciaArribaIntercambiaConElAnterior() = runBlocking {
        val (routineId, dayId) = freshRoutineWithDay()
        repository.addExerciseToDay(routineId, dayId, "press-de-pecho")
        repository.addExerciseToDay(routineId, dayId, "remo")
        repository.addExerciseToDay(routineId, dayId, "curl")
        val before = repository.observeRoutine(routineId).first()!!.days.single().exercises
        val curlId = before[2].id

        repository.moveExercise(routineId, dayId, curlId, MoveDirection.UP)

        val after = repository.observeRoutine(routineId).first()!!.days.single().exercises
        assertEquals(listOf("press-de-pecho", "curl", "remo"), after.map { it.exercise.id })
    }

    @Test
    fun moverElPrimerEjercicioHaciaArribaNoHaceNada() = runBlocking {
        val (routineId, dayId) = freshRoutineWithDay()
        repository.addExerciseToDay(routineId, dayId, "press-de-pecho")
        repository.addExerciseToDay(routineId, dayId, "remo")
        val before = repository.observeRoutine(routineId).first()!!.days.single().exercises
        val firstId = before[0].id

        repository.moveExercise(routineId, dayId, firstId, MoveDirection.UP)

        val after = repository.observeRoutine(routineId).first()!!.days.single().exercises
        assertEquals(listOf("press-de-pecho", "remo"), after.map { it.exercise.id })
    }
}
