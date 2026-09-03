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
    weightKg: Double = 80.0,
    neckCm: Double? = null,
    waistCm: Double? = null,
    hipCm: Double? = null,
) = BodyMeasurementEntry(
    id = "e1", date = LocalDate.of(2026, 9, 1), weightKg = weightKg,
    neckCm = neckCm, waistCm = waistCm, hipCm = hipCm,
)

class BodyMetricsTest {

    @Test
    fun `imc se calcula con peso y altura`() {
        val bmi = BodyMetrics.bmi(weightKg = 81.0, heightCm = 180.0)

        assertEquals(25.0, bmi, 0.01)
    }

    @Test
    fun `clasificacion OMS cubre las 4 categorias`() {
        assertEquals(BmiCategory.BAJO_PESO, BodyMetrics.bmiCategory(18.0))
        assertEquals(BmiCategory.NORMAL, BodyMetrics.bmiCategory(22.0))
        assertEquals(BmiCategory.SOBREPESO, BodyMetrics.bmiCategory(27.0))
        assertEquals(BmiCategory.OBESIDAD, BodyMetrics.bmiCategory(32.0))
    }

    @Test
    fun `porcentaje de grasa nulo si falta el perfil`() {
        val profile = BodyProfile()
        val result = BodyMetrics.bodyFatPercent(profile, entry(neckCm = 38.0, waistCm = 85.0))

        assertNull(result)
    }

    @Test
    fun `porcentaje de grasa nulo si a la medicion le falta cuello o cintura`() {
        val profile = BodyProfile(biologicalSex = BiologicalSex.MALE, heightCm = 180.0)

        assertNull(BodyMetrics.bodyFatPercent(profile, entry(neckCm = 38.0, waistCm = null)))
        assertNull(BodyMetrics.bodyFatPercent(profile, entry(neckCm = null, waistCm = 85.0)))
    }

    @Test
    fun `porcentaje de grasa masculino se calcula con perfil y medicion completos`() {
        val profile = BodyProfile(biologicalSex = BiologicalSex.MALE, heightCm = 180.0)
        val result = BodyMetrics.bodyFatPercent(profile, entry(neckCm = 38.0, waistCm = 85.0))

        assertTrue(result != null && result > 0.0 && result < 50.0)
    }

    @Test
    fun `porcentaje de grasa femenino requiere ademas la cadera`() {
        val profile = BodyProfile(biologicalSex = BiologicalSex.FEMALE, heightCm = 165.0)

        assertNull(BodyMetrics.bodyFatPercent(profile, entry(neckCm = 32.0, waistCm = 70.0, hipCm = null)))

        val result = BodyMetrics.bodyFatPercent(profile, entry(neckCm = 32.0, waistCm = 70.0, hipCm = 95.0))
        assertTrue(result != null && result > 0.0 && result < 50.0)
    }
}
