package com.example.fitswap.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitswap.data.local.entity.BodyMeasurementEntity
import com.example.fitswap.data.local.entity.BodyProfileEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BodyMeasurementDaoTest {

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

    @Test
    fun insertarUnaMedicionSePuedeLeerDeVuelta() = runBlocking {
        database.bodyMeasurementDao().insert(
            BodyMeasurementEntity(
                id = "m1", date = LocalDate.of(2026, 9, 2), weightKg = 80.0,
                neckCm = 38.0, waistCm = 85.0, hipCm = null, chestCm = null, armCm = null, legCm = null, calfCm = null,
                gluteCm = null, forearmCm = null, shoulderCm = null, wristCm = null,
            )
        )

        val measurements = database.bodyMeasurementDao().observeMeasurements().first()

        assertEquals(1, measurements.size)
        assertEquals(80.0, measurements.single().weightKg, 0.0)
        assertEquals(38.0, measurements.single().neckCm)
        assertNull(measurements.single().hipCm)
    }

    @Test
    fun elGluteoAntebrazoHombroYMunecaHacenRoundTrip() = runBlocking {
        database.bodyMeasurementDao().insert(
            BodyMeasurementEntity(
                id = "m2", date = LocalDate.of(2026, 9, 3), weightKg = 80.0,
                neckCm = null, waistCm = null, hipCm = null, chestCm = null, armCm = null, legCm = null, calfCm = null,
                gluteCm = 98.0, forearmCm = 27.0, shoulderCm = 112.0, wristCm = 16.5,
            )
        )

        val measurement = database.bodyMeasurementDao().observeMeasurements().first().single()

        assertEquals(98.0, measurement.gluteCm)
        assertEquals(27.0, measurement.forearmCm)
        assertEquals(112.0, measurement.shoulderCm)
        assertEquals(16.5, measurement.wristCm)
    }

    @Test
    fun elPerfilVacioDevuelveNuloAntesDeGuardarNada() = runBlocking {
        val profile = database.bodyProfileDao().observeProfile().first()

        assertNull(profile)
    }

    @Test
    fun guardarElPerfilDosVecesSobreescribeLaMismaFilaSingleton() = runBlocking {
        database.bodyProfileDao().upsert(BodyProfileEntity(biologicalSex = "MALE", heightCm = 180.0))
        database.bodyProfileDao().upsert(BodyProfileEntity(biologicalSex = "FEMALE", heightCm = 165.0))

        val profile = database.bodyProfileDao().observeProfile().first()

        assertEquals("FEMALE", profile?.biologicalSex)
        assertEquals(165.0, profile?.heightCm)
    }
}
