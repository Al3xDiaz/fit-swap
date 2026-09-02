package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.PlannedSet
import com.example.fitswap.domain.model.RoutineExercise
import com.example.fitswap.domain.model.SetType

/**
 * Turns a [RoutineExercise]'s static set scheme (from `rutina_semanal_optimizada.md`, transcribed
 * into [RoutineExercise.approachSets]/[RoutineExercise.effectiveSets]/labels) into an ordered,
 * linear list of [PlannedSet]s: 1 warmup set, then the approach sets, then the effective sets.
 *
 * Weights are expressed as a factor of the "peso efectivo" (see [suggestedEffectiveWeight]) instead
 * of an absolute kg value, so that editing peso efectivo live recomputes the *current* (not yet
 * logged) stage's suggestion without touching already-logged, already-fixed stages.
 */
object SetPlanner {

    private const val DEFAULT_EFFECTIVE_WEIGHT_KG = 1.0
    private const val WARMUP_WEIGHT_FACTOR = 0.5
    private const val DEFAULT_APPROACH_WEIGHT_FACTOR = 0.6
    private const val DEFAULT_REPS = 10
    private const val DEFAULT_REST_SECONDS = 60

    private val firstNumberRegex = Regex("\\d+")

    /** Peso efectivo sugerido: el último registrado para el ejercicio, o 1 kg si no hay historial. */
    fun suggestedEffectiveWeight(lastLoggedWeightKg: Double?): Double =
        lastLoggedWeightKg ?: DEFAULT_EFFECTIVE_WEIGHT_KG

    fun buildPlan(routineExercise: RoutineExercise): List<PlannedSet> {
        val approachWeightFactor = parseApproachWeightFactor(routineExercise.approachGuideline)
        val effectiveReps = parseReps(routineExercise.effectiveRepsLabel)
        val approachReps = routineExercise.approachGuideline?.let(::parseReps) ?: effectiveReps
        val restSeconds = parseRestSeconds(routineExercise.restLabel)

        val warmup = PlannedSet(
            type = SetType.WARMUP,
            stageNumber = 1,
            totalInType = 1,
            plannedReps = DEFAULT_REPS,
            weightFactor = WARMUP_WEIGHT_FACTOR,
            restSeconds = restSeconds,
        )
        val approach = (1..routineExercise.approachSets).map { stageNumber ->
            PlannedSet(
                type = SetType.APPROACH,
                stageNumber = stageNumber,
                totalInType = routineExercise.approachSets,
                plannedReps = approachReps,
                weightFactor = approachWeightFactor,
                restSeconds = restSeconds,
            )
        }
        val effective = (1..routineExercise.effectiveSets).map { stageNumber ->
            PlannedSet(
                type = SetType.EFFECTIVE,
                stageNumber = stageNumber,
                totalInType = routineExercise.effectiveSets,
                plannedReps = effectiveReps,
                weightFactor = 1.0,
                restSeconds = restSeconds,
            )
        }
        return listOf(warmup) + approach + effective
    }

    internal fun parseReps(label: String): Int =
        firstNumberRegex.find(label)?.value?.toIntOrNull() ?: DEFAULT_REPS

    internal fun parseApproachWeightFactor(guideline: String?): Double =
        guideline
            ?.let { firstNumberRegex.find(it)?.value?.toIntOrNull() }
            ?.let { percentage -> percentage / 100.0 }
            ?: DEFAULT_APPROACH_WEIGHT_FACTOR

    internal fun parseRestSeconds(label: String): Int {
        val number = firstNumberRegex.find(label)?.value?.toIntOrNull() ?: DEFAULT_REST_SECONDS
        return if (label.contains("min")) number * 60 else number
    }
}
