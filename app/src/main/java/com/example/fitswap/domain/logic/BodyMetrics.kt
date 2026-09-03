package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile
import kotlin.math.log10

enum class BmiCategory { BAJO_PESO, NORMAL, SOBREPESO, OBESIDAD }

/** Cálculos derivados de una [BodyMeasurementEntry] + el [BodyProfile] del usuario. Sin estado, sin
 * dependencias de Android — testeable como funciones puras (mismo criterio que `SetPlanner`). */
object BodyMetrics {

    fun bmi(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    /** Clasificación estándar de la OMS sobre el valor de IMC. */
    fun bmiCategory(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.BAJO_PESO
        bmi < 25.0 -> BmiCategory.NORMAL
        bmi < 30.0 -> BmiCategory.SOBREPESO
        else -> BmiCategory.OBESIDAD
    }

    /**
     * % de grasa corporal vía la fórmula US Navy (sin calibre, solo cinta métrica) — devuelve
     * `null` si falta algún dato requerido (sexo/altura del perfil; cuello/cintura de la medición,
     * más cadera si el sexo es femenino) en vez de inventar un valor. Mismo criterio que "peso
     * sugerido = 1 kg sin historial" de M3: nunca ocultar que falta un dato mostrando un número.
     */
    fun bodyFatPercent(profile: BodyProfile, entry: BodyMeasurementEntry): Double? {
        val sex = profile.biologicalSex ?: return null
        val heightCm = profile.heightCm ?: return null
        val neckCm = entry.neckCm ?: return null
        val waistCm = entry.waistCm ?: return null

        return when (sex) {
            BiologicalSex.MALE -> {
                if (waistCm <= neckCm) return null
                495.0 / (1.0324 - 0.19077 * log10(waistCm - neckCm) + 0.15456 * log10(heightCm)) - 450.0
            }
            BiologicalSex.FEMALE -> {
                val hipCm = entry.hipCm ?: return null
                if (waistCm + hipCm <= neckCm) return null
                495.0 / (1.29579 - 0.35004 * log10(waistCm + hipCm - neckCm) + 0.22100 * log10(heightCm)) - 450.0
            }
        }
    }
}
