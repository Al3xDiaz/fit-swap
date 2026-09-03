package com.example.fitswap.domain.logic

import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.RoutineExercise
import com.example.fitswap.domain.model.SetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetPlannerTest {

    private val exercise = Exercise(
        id = "press-de-pecho",
        name = "Press de pecho",
        muscleGroup = "Pecho",
        equipment = "Máquina/Mancuerna",
    )

    @Test
    fun `peso efectivo sugerido es el ultimo registrado si hay historial`() {
        assertEquals(60.0, SetPlanner.suggestedEffectiveWeight(60.0), 0.0)
    }

    @Test
    fun `peso efectivo sugerido es 1kg cuando no hay historial previo`() {
        assertEquals(1.0, SetPlanner.suggestedEffectiveWeight(null), 0.0)
    }

    @Test
    fun `el plan empieza con calentamiento y sigue con aproximacion y efectivas en orden`() {
        val routineExercise = RoutineExercise(
            id = "martes-press-de-pecho",
            exercise = exercise,
            approachSets = 1,
            effectiveSets = 4,
            effectiveRepsLabel = "8–12",
            approachGuideline = "60–65% × 6–8",
            restLabel = "2–3 min",
        )

        val plan = SetPlanner.buildPlan(routineExercise)

        assertEquals(6, plan.size)
        assertEquals(SetType.WARMUP, plan[0].type)
        assertEquals(SetType.APPROACH, plan[1].type)
        assertEquals(SetType.EFFECTIVE, plan[2].type)
        assertEquals(SetType.EFFECTIVE, plan[5].type)
        assertEquals(4, plan.count { it.type == SetType.EFFECTIVE })
    }

    @Test
    fun `un ejercicio sin series de aproximacion no genera etapas de aproximacion`() {
        val routineExercise = RoutineExercise(
            id = "martes-elevaciones-laterales",
            exercise = exercise,
            approachSets = 0,
            effectiveSets = 4,
            effectiveRepsLabel = "12–15",
            restLabel = "60–75 s",
        )

        val plan = SetPlanner.buildPlan(routineExercise)

        assertEquals(0, plan.count { it.type == SetType.APPROACH })
        assertEquals(SetType.WARMUP, plan.first().type)
    }

    @Test
    fun `parseReps toma el primer numero de un rango de reps`() {
        assertEquals(8, SetPlanner.parseReps("8–12"))
        assertEquals(12, SetPlanner.parseReps("12–15"))
    }

    @Test
    fun `parseReps usa un valor por defecto cuando la etiqueta no tiene numeros`() {
        assertEquals(10, SetPlanner.parseReps("Cerca del fallo técnico"))
    }

    @Test
    fun `parseApproachWeightFactor toma el primer porcentaje de la guia`() {
        assertEquals(0.6, SetPlanner.parseApproachWeightFactor("60% × 6–8"), 0.0)
        assertEquals(0.5, SetPlanner.parseApproachWeightFactor("50% × 6 → 70% × 4"), 0.0)
    }

    @Test
    fun `parseApproachWeightFactor usa un valor por defecto cuando no hay guia`() {
        assertEquals(0.6, SetPlanner.parseApproachWeightFactor(null), 0.0)
    }

    @Test
    fun `el descanso de cada etapa usa el valor fijo pasado, no el restLabel del ejercicio`() {
        val routineExercise = RoutineExercise(
            id = "martes-press-de-pecho",
            exercise = exercise,
            approachSets = 1,
            effectiveSets = 2,
            effectiveRepsLabel = "8–12",
            restLabel = "2–3 min", // ignorado a propósito: el timer es global (Configuraciones, M9).
        )

        val plan = SetPlanner.buildPlan(routineExercise, restSeconds = 45)

        assertTrue(plan.all { it.restSeconds == 45 })
    }

    @Test
    fun `sin restSeconds explicito usa el default fijo del planificador`() {
        val routineExercise = RoutineExercise(
            id = "martes-press-de-pecho",
            exercise = exercise,
            approachSets = 0,
            effectiveSets = 1,
            effectiveRepsLabel = "8–12",
            restLabel = "2–3 min",
        )

        val plan = SetPlanner.buildPlan(routineExercise)

        assertTrue(plan.all { it.restSeconds == SetPlanner.DEFAULT_REST_SECONDS })
    }
}
