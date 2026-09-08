package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun entry(
    shoulderCm: Double? = null,
    waistCm: Double? = null,
    hipCm: Double? = null,
    gluteCm: Double? = null,
    forearmCm: Double? = null,
    wristCm: Double? = null,
    legCm: Double? = null,
    calfCm: Double? = null,
) = BodyMeasurementEntry(
    id = "e1", date = LocalDate.of(2026, 9, 1), weightKg = 80.0,
    shoulderCm = shoulderCm, waistCm = waistCm, hipCm = hipCm, gluteCm = gluteCm,
    forearmCm = forearmCm, wristCm = wristCm, legCm = legCm, calfCm = calfCm,
)

private val EMPTY_PROFILE = BodyProfile()
private val MALE_PROFILE = BodyProfile(biologicalSex = BiologicalSex.MALE, heightCm = 180.0)
private val FEMALE_PROFILE = BodyProfile(biologicalSex = BiologicalSex.FEMALE, heightCm = 165.0)

class MuscleFocusAnalyzerTest {

    @Test
    fun `analyze devuelve los 5 ratios sin duplicados`() {
        val ratios = MuscleFocusAnalyzer.analyze(entry(), EMPTY_PROFILE)

        assertEquals(RatioId.entries.toSet(), ratios.map { it.id }.toSet())
        assertEquals(5, ratios.size)
    }

    @Test
    fun `hombro cintura bajo el rango`() {
        val ratio = ratioById(entry(shoulderCm = 100.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.SHOULDER_WAIST) // 1.25

        assertEquals(RatioStatus.BAJO, ratio.status)
    }

    @Test
    fun `hombro cintura en rango en el borde bajo inclusive`() {
        val ratio = ratioById(entry(shoulderCm = 116.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.SHOULDER_WAIST) // 1.45

        assertEquals(RatioStatus.EN_RANGO, ratio.status)
    }

    @Test
    fun `hombro cintura en rango en el borde alto inclusive`() {
        val ratio = ratioById(entry(shoulderCm = 140.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.SHOULDER_WAIST) // 1.75

        assertEquals(RatioStatus.EN_RANGO, ratio.status)
    }

    @Test
    fun `hombro cintura alto sobre el rango`() {
        val ratio = ratioById(entry(shoulderCm = 150.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.SHOULDER_WAIST) // 1.875

        assertEquals(RatioStatus.ALTO, ratio.status)
    }

    @Test
    fun `hombro cintura sin cintura queda con valor y estado nulos`() {
        val ratio = ratioById(entry(shoulderCm = 100.0), EMPTY_PROFILE, RatioId.SHOULDER_WAIST)

        assertNull(ratio.value)
        assertNull(ratio.status)
        assertTrue(ratio.interpretation.contains("cintura"))
    }

    @Test
    fun `gluteo cintura cubre los 3 estados`() {
        assertEquals(RatioStatus.BAJO, ratioById(entry(gluteCm = 90.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.GLUTE_WAIST).status) // 1.125
        assertEquals(RatioStatus.EN_RANGO, ratioById(entry(gluteCm = 108.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.GLUTE_WAIST).status) // 1.35
        assertEquals(RatioStatus.ALTO, ratioById(entry(gluteCm = 130.0, waistCm = 80.0), EMPTY_PROFILE, RatioId.GLUTE_WAIST).status) // 1.625
    }

    @Test
    fun `antebrazo muneca cubre los 3 estados`() {
        assertEquals(RatioStatus.BAJO, ratioById(entry(forearmCm = 28.0, wristCm = 17.0), EMPTY_PROFILE, RatioId.FOREARM_WRIST).status) // 1.647
        assertEquals(RatioStatus.EN_RANGO, ratioById(entry(forearmCm = 32.0, wristCm = 17.0), EMPTY_PROFILE, RatioId.FOREARM_WRIST).status) // 1.88
        assertEquals(RatioStatus.ALTO, ratioById(entry(forearmCm = 40.0, wristCm = 17.0), EMPTY_PROFILE, RatioId.FOREARM_WRIST).status) // 2.35
    }

    @Test
    fun `muslo pantorrilla cubre los 3 estados`() {
        assertEquals(RatioStatus.BAJO, ratioById(entry(legCm = 45.0, calfCm = 38.0), EMPTY_PROFILE, RatioId.THIGH_CALF).status) // 1.18
        assertEquals(RatioStatus.EN_RANGO, ratioById(entry(legCm = 57.0, calfCm = 38.0), EMPTY_PROFILE, RatioId.THIGH_CALF).status) // 1.5
        assertEquals(RatioStatus.ALTO, ratioById(entry(legCm = 72.0, calfCm = 38.0), EMPTY_PROFILE, RatioId.THIGH_CALF).status) // 1.89
    }

    @Test
    fun `cintura cadera usa los cortes de hombre`() {
        // waist/hip = 0.95 -> justo en el borde alto, EN_RANGO para hombre.
        val enRango = ratioById(entry(waistCm = 95.0, hipCm = 100.0), MALE_PROFILE, RatioId.WAIST_HIP)
        assertEquals(RatioStatus.EN_RANGO, enRango.status)

        // waist/hip = 1.0 -> por encima del corte alto de hombre (0.95).
        val alto = ratioById(entry(waistCm = 100.0, hipCm = 100.0), MALE_PROFILE, RatioId.WAIST_HIP)
        assertEquals(RatioStatus.ALTO, alto.status)
    }

    @Test
    fun `cintura cadera usa los cortes de mujer`() {
        // waist/hip = 0.85 -> justo en el borde alto, EN_RANGO para mujer.
        val enRango = ratioById(entry(waistCm = 85.0, hipCm = 100.0), FEMALE_PROFILE, RatioId.WAIST_HIP)
        assertEquals(RatioStatus.EN_RANGO, enRango.status)

        // waist/hip = 0.9 -> por encima del corte alto de mujer (0.85).
        val alto = ratioById(entry(waistCm = 90.0, hipCm = 100.0), FEMALE_PROFILE, RatioId.WAIST_HIP)
        assertEquals(RatioStatus.ALTO, alto.status)
    }

    @Test
    fun `cintura cadera sin sexo en el perfil expone el valor pero sin estado`() {
        val ratio = ratioById(entry(waistCm = 90.0, hipCm = 100.0), EMPTY_PROFILE, RatioId.WAIST_HIP)

        assertEquals(0.9, ratio.value!!, 0.0001)
        assertNull(ratio.status)
        assertTrue(ratio.interpretation.contains("sexo biológico"))
    }

    @Test
    fun `cintura cadera sin cintura o cadera queda con valor y estado nulos, sin importar el sexo`() {
        val ratio = ratioById(entry(waistCm = 90.0), MALE_PROFILE, RatioId.WAIST_HIP)

        assertNull(ratio.value)
        assertNull(ratio.status)
    }

    private fun ratioById(entry: BodyMeasurementEntry, profile: BodyProfile, id: RatioId): MuscleFocusRatio =
        MuscleFocusAnalyzer.analyze(entry, profile).single { it.id == id }
}
