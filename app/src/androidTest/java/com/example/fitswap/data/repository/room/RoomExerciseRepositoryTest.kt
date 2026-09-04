package com.example.fitswap.data.repository.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.FitSwapDatabase
import com.example.fitswap.data.local.entity.GalleryItemEntity
import com.example.fitswap.domain.model.Exercise
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomExerciseRepositoryTest {

    private lateinit var database: FitSwapDatabase
    private lateinit var repository: RoomExerciseRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitSwapDatabase::class.java).build()
        repository = RoomExerciseRepository(
            database,
            database.exerciseDao(),
            database.galleryDao(),
            database.historyDao(),
            database.noteDao(),
            database.substituteDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun actualizarUnEjercicioNoBorraSuGaleriaPorCascada() = runBlocking {
        val exercise = Exercise("press-de-pecho", "Press de pecho", "Pecho", "Máquina")
        repository.addExercise(exercise)
        database.galleryDao().insert(GalleryItemEntity("g1", exercise.id, "content://foo", isVideo = false))

        repository.updateExercise(exercise.copy(name = "Press de pecho inclinado"))

        val gallery = database.galleryDao().observeGallery(exercise.id).first()
        assertEquals(1, gallery.size)
        assertEquals("g1", gallery.single().id)
    }
}
