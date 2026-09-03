package com.example.fitswap.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.entity.ExerciseEntity
import com.example.fitswap.data.local.entity.RoutineDayEntity
import com.example.fitswap.data.local.entity.RoutineEntity
import com.example.fitswap.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {

    private lateinit var database: FitSwapDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun seedOneRoutineDayExercise() = runBlocking {
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

    @Test
    fun insertarRutinaConDiaYEjercicioSePuedeLeerDeVuelta() = runBlocking {
        seedOneRoutineDayExercise()

        val routines = database.routineDao().observeRoutines().first()
        val days = database.routineDao().observeDays().first()
        val exercises = database.routineDao().observeRoutineExercises().first()

        assertEquals(1, routines.size)
        assertEquals("Rutina de prueba", routines.single().name)
        assertEquals(1, days.size)
        assertEquals("r1", days.single().routineId)
        assertEquals(1, exercises.size)
        assertEquals("d1", exercises.single().dayId)
    }

    @Test
    fun borrarUnDiaBorraEnCascadaSusEjerciciosEnRutina() = runBlocking {
        seedOneRoutineDayExercise()

        database.routineDao().removeDay("d1")

        val days = database.routineDao().observeDays().first()
        val exercises = database.routineDao().observeRoutineExercises().first()
        assertTrue("el día debería haberse borrado", days.isEmpty())
        assertTrue("borrar el día debería arrastrar en cascada sus ejercicios-en-rutina", exercises.isEmpty())
    }

    @Test
    fun borrarUnaRutinaBorraEnCascadaSusDiasYEjercicios() = runBlocking {
        seedOneRoutineDayExercise()

        database.routineDao().deleteRoutine("r1")

        val routines = database.routineDao().observeRoutines().first()
        val days = database.routineDao().observeDays().first()
        val exercises = database.routineDao().observeRoutineExercises().first()
        assertTrue(routines.isEmpty())
        assertTrue("borrar la rutina debería arrastrar en cascada sus días", days.isEmpty())
        assertTrue("borrar la rutina debería arrastrar en cascada los ejercicios de sus días", exercises.isEmpty())
    }

    @Test
    fun deleteRoutineEsNoOpParaLaRutinaPorDefecto() = runBlocking {
        database.routineDao().insertRoutine(RoutineEntity(id = "default", name = "Rutina por defecto", isDefault = true))

        database.routineDao().deleteRoutine("default")

        val routines = database.routineDao().observeRoutines().first()
        assertEquals(1, routines.size)
    }

    @Test
    fun logSetSeBorraEnCascadaAlBorrarSuEjercicioEnRutina() = runBlocking {
        seedOneRoutineDayExercise()
        database.setDao().insert(
            com.example.fitswap.data.local.entity.LoggedSetEntity(
                id = "s1", routineExerciseId = "re1", type = com.example.fitswap.domain.model.SetType.EFFECTIVE, reps = 10, weightKg = 50.0
            )
        )

        database.routineDao().removeRoutineExercise("re1")

        val loggedSets = database.setDao().observeLoggedSets("re1").first()
        assertTrue("borrar el ejercicio-en-rutina debería arrastrar en cascada sus series registradas", loggedSets.isEmpty())
    }
}
