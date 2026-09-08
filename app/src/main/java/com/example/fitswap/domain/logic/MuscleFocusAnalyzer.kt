package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.BiologicalSex
import com.example.fitswap.domain.model.BodyMeasurementEntry
import com.example.fitswap.domain.model.BodyProfile

enum class RatioId { SHOULDER_WAIST, WAIST_HIP, GLUTE_WAIST, FOREARM_WRIST, THIGH_CALF }
enum class RatioStatus { BAJO, EN_RANGO, ALTO }

data class MuscleFocusRatio(
    val id: RatioId,
    val label: String,
    val value: Double?,
    val status: RatioStatus?,
    val interpretation: String,
)

private const val SHOULDER_WAIST_LOW = 1.45
private const val SHOULDER_WAIST_HIGH = 1.75
private const val GLUTE_WAIST_LOW = 1.25
private const val GLUTE_WAIST_HIGH = 1.45
private const val FOREARM_WRIST_LOW = 1.8
private const val FOREARM_WRIST_HIGH = 2.1
private const val THIGH_CALF_LOW = 1.4
private const val THIGH_CALF_HIGH = 1.7

// Cortes de cintura/cadera de referencia OMS para riesgo cardiometabólico (no específicos de esta
// app): por debajo del corte bajo, riesgo bajo; entre ambos, riesgo moderado; por encima del alto,
// riesgo alto.
private const val WAIST_HIP_MALE_LOW = 0.90
private const val WAIST_HIP_MALE_HIGH = 0.95
private const val WAIST_HIP_FEMALE_LOW = 0.80
private const val WAIST_HIP_FEMALE_HIGH = 0.85

/**
 * Recomendaciones de enfoque muscular basadas **solo en proporciones entre medidas** (no en
 * historial de entrenamiento) — 5 ratios con rangos de referencia general, no un diagnóstico
 * médico ni una evaluación de precisión clínica. Nunca estima un campo faltante (mismo criterio
 * que [BodyMetrics.bodyFatPercent]): si falta alguna medida requerida, el ratio se devuelve con
 * `value`/`status` nulos y una interpretación pidiendo medir lo que falta.
 *
 * Convención de bordes: `[low, high]` inclusive → [RatioStatus.EN_RANGO]; `< low` → BAJO;
 * `> high` → ALTO.
 */
object MuscleFocusAnalyzer {

    fun analyze(entry: BodyMeasurementEntry, profile: BodyProfile): List<MuscleFocusRatio> = listOf(
        shoulderWaist(entry),
        waistHip(entry, profile),
        gluteWaist(entry),
        forearmWrist(entry),
        thighCalf(entry),
    )

    private fun shoulderWaist(entry: BodyMeasurementEntry): MuscleFocusRatio {
        val shoulder = entry.shoulderCm
        val waist = entry.waistCm
        if (shoulder == null || waist == null) {
            return missing(RatioId.SHOULDER_WAIST, "Hombro / cintura", shoulder to "hombro", waist to "cintura")
        }
        val value = shoulder / waist
        val status = statusFor(value, SHOULDER_WAIST_LOW, SHOULDER_WAIST_HIGH)
        val interpretation = when (status) {
            RatioStatus.BAJO -> "Por debajo del rango de referencia — sumar hombro y espalda ayuda a marcar más la forma en V."
            RatioStatus.EN_RANGO -> "Dentro del rango de referencia."
            RatioStatus.ALTO -> "Por encima del rango de referencia."
        }
        return MuscleFocusRatio(RatioId.SHOULDER_WAIST, "Hombro / cintura", value, status, interpretation)
    }

    private fun waistHip(entry: BodyMeasurementEntry, profile: BodyProfile): MuscleFocusRatio {
        val waist = entry.waistCm
        val hip = entry.hipCm
        if (waist == null || hip == null) {
            return missing(RatioId.WAIST_HIP, "Cintura / cadera", waist to "cintura", hip to "cadera")
        }
        val value = waist / hip
        val sex = profile.biologicalSex
            ?: return MuscleFocusRatio(
                RatioId.WAIST_HIP, "Cintura / cadera", value, null,
                "Completá tu sexo biológico en Configuración para interpretar este valor.",
            )
        val (low, high) = when (sex) {
            BiologicalSex.MALE -> WAIST_HIP_MALE_LOW to WAIST_HIP_MALE_HIGH
            BiologicalSex.FEMALE -> WAIST_HIP_FEMALE_LOW to WAIST_HIP_FEMALE_HIGH
        }
        val status = statusFor(value, low, high)
        val interpretation = when (status) {
            RatioStatus.BAJO -> "Por debajo del rango de referencia de la OMS (riesgo cardiometabólico bajo)."
            RatioStatus.EN_RANGO -> "Dentro del rango de referencia de la OMS (riesgo cardiometabólico moderado)."
            RatioStatus.ALTO -> "Por encima del rango de referencia de la OMS (riesgo cardiometabólico alto) — más acondicionamiento general puede ayudar a bajarlo."
        }
        return MuscleFocusRatio(RatioId.WAIST_HIP, "Cintura / cadera", value, status, interpretation)
    }

    private fun gluteWaist(entry: BodyMeasurementEntry): MuscleFocusRatio {
        val glute = entry.gluteCm
        val waist = entry.waistCm
        if (glute == null || waist == null) {
            return missing(RatioId.GLUTE_WAIST, "Glúteo / cintura", glute to "glúteo", waist to "cintura")
        }
        val value = glute / waist
        val status = statusFor(value, GLUTE_WAIST_LOW, GLUTE_WAIST_HIGH)
        val interpretation = when (status) {
            RatioStatus.BAJO -> "Por debajo del rango de referencia — hip thrust, sentadilla y peso muerto rumano suman volumen de glúteo."
            RatioStatus.EN_RANGO -> "Dentro del rango de referencia."
            RatioStatus.ALTO -> "Por encima del rango de referencia."
        }
        return MuscleFocusRatio(RatioId.GLUTE_WAIST, "Glúteo / cintura", value, status, interpretation)
    }

    private fun forearmWrist(entry: BodyMeasurementEntry): MuscleFocusRatio {
        val forearm = entry.forearmCm
        val wrist = entry.wristCm
        if (forearm == null || wrist == null) {
            return missing(RatioId.FOREARM_WRIST, "Antebrazo / muñeca", forearm to "antebrazo", wrist to "muñeca")
        }
        val value = forearm / wrist
        val status = statusFor(value, FOREARM_WRIST_LOW, FOREARM_WRIST_HIGH)
        val interpretation = when (status) {
            RatioStatus.BAJO -> "Por debajo del rango de referencia — curl de muñeca y dead hangs suman fuerza de antebrazo/agarre."
            RatioStatus.EN_RANGO -> "Dentro del rango de referencia."
            RatioStatus.ALTO -> "Por encima del rango de referencia."
        }
        return MuscleFocusRatio(RatioId.FOREARM_WRIST, "Antebrazo / muñeca", value, status, interpretation)
    }

    private fun thighCalf(entry: BodyMeasurementEntry): MuscleFocusRatio {
        val thigh = entry.legCm
        val calf = entry.calfCm
        if (thigh == null || calf == null) {
            return missing(RatioId.THIGH_CALF, "Muslo / pantorrilla", thigh to "muslo", calf to "pantorrilla")
        }
        val value = thigh / calf
        val status = statusFor(value, THIGH_CALF_LOW, THIGH_CALF_HIGH)
        val interpretation = when (status) {
            RatioStatus.BAJO -> "Por debajo del rango de referencia — cuádriceps e isquiotibiales (sentadilla, prensa, curl femoral) pueden ayudar a equilibrar la proporción."
            RatioStatus.EN_RANGO -> "Dentro del rango de referencia."
            RatioStatus.ALTO -> "Por encima del rango de referencia: la pantorrilla puede estar quedando rezagada respecto al muslo."
        }
        return MuscleFocusRatio(RatioId.THIGH_CALF, "Muslo / pantorrilla", value, status, interpretation)
    }

    private fun statusFor(value: Double, low: Double, high: Double): RatioStatus = when {
        value < low -> RatioStatus.BAJO
        value > high -> RatioStatus.ALTO
        else -> RatioStatus.EN_RANGO
    }

    private fun missing(id: RatioId, label: String, vararg fields: Pair<Double?, String>): MuscleFocusRatio {
        val missingLabels = fields.filter { it.first == null }.map { it.second }
        return MuscleFocusRatio(id, label, null, null, "Medí ${missingLabels.joinToString(" y ")} para ver esto.")
    }
}
