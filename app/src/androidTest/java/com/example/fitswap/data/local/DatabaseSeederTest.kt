package com.example.fitswap.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.entity.toEntity
import com.example.fitswap.data.repository.fake.ExerciseCatalog
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseSeederTest {

    private lateinit var database: FitSwapDatabase
    private lateinit var seeder: DatabaseSeeder

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
        seeder = DatabaseSeeder(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sembrarUnaBaseVaciaInsertaTodoElCatalogo() = runBlocking {
        seeder.seedIfNeeded()

        val ids = database.exerciseDao().allIds().toSet()
        assertEquals(ExerciseCatalog.allExercises.map { it.id }.toSet(), ids)
    }

    @Test
    fun sincronizarUnaInstalacionExistenteAgregaSoloLosNuevosSinPisarLosExistentes() = runBlocking {
        // Simula una instalación sembrada antes de que se agregaran los 4 ejercicios de cardio
        // nuevos, con un ejercicio existente cuyo nombre el usuario editó.
        val cardioNuevoIds = setOf(
            ExerciseCatalog.eliptica.id,
            ExerciseCatalog.remoMaquina.id,
            ExerciseCatalog.escaladora.id,
            ExerciseCatalog.bicicletaEstatica.id,
        )
        val editedName = "Caminar en cinta (editado por el usuario)"
        val staleEntities = ExerciseCatalog.allExercises
            .filterNot { it.id in cardioNuevoIds }
            .map { exercise ->
                if (exercise.id == ExerciseCatalog.caminarEnCinta.id) {
                    exercise.copy(name = editedName).toEntity()
                } else {
                    exercise.toEntity()
                }
            }
        database.exerciseDao().insertAll(staleEntities)

        seeder.seedIfNeeded()

        val ids = database.exerciseDao().allIds().toSet()
        assertEquals(ExerciseCatalog.allExercises.map { it.id }.toSet(), ids)
        val caminarEnCinta = database.exerciseDao().getExercise(ExerciseCatalog.caminarEnCinta.id)
        assertEquals(editedName, caminarEnCinta?.name)
    }

    @Test
    fun sincronizarDosVecesSeguidasNoDuplicaNada() = runBlocking {
        seeder.seedIfNeeded()

        seeder.seedIfNeeded()

        val ids = database.exerciseDao().allIds()
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(ExerciseCatalog.allExercises.size, ids.size)
    }
}
