package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.domain.model.RoutineExercise
import java.time.DayOfWeek

private fun routineExercise(
    dayId: String,
    exercise: Exercise,
    approachSets: Int,
    effectiveSets: Int,
    effectiveRepsLabel: String,
    approachGuideline: String? = null,
    restLabel: String,
    usesStraps: Boolean = false,
    notes: String? = null,
) = RoutineExercise(
    id = "$dayId-${exercise.id}",
    exercise = exercise,
    approachSets = approachSets,
    effectiveSets = effectiveSets,
    effectiveRepsLabel = effectiveRepsLabel,
    approachGuideline = approachGuideline,
    restLabel = restLabel,
    usesStraps = usesStraps,
    notes = notes,
)

private fun pushDay(routineId: String): RoutineDay {
    val dayId = "$routineId-martes"
    return RoutineDay(
        id = dayId,
        name = "Martes — Push",
        dayOfWeek = DayOfWeek.TUESDAY,
        exercises = listOf(
            routineExercise(dayId, ExerciseCatalog.elevacionesLaterales, approachSets = 0, effectiveSets = 4, effectiveRepsLabel = "12–15", restLabel = "60–75 s", notes = "Deltoide lateral primero para priorizarlo. Brazo ligeramente hacia delante."),
            routineExercise(dayId, ExerciseCatalog.pressMilitarMaquina, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60% × 6–8", restLabel = "2–3 min", notes = "Ajustar asiento para que los agarres queden aproximadamente a nivel de la barbilla; codos ~45°."),
            routineExercise(dayId, ExerciseCatalog.pressDePecho, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6–8", restLabel = "2–3 min", notes = "Máquina o mancuernas. Control y escápulas estables."),
            routineExercise(dayId, ExerciseCatalog.aperturasDePecho, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Recorrido cómodo y controlado."),
            routineExercise(dayId, ExerciseCatalog.fondosParaTriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "Cerca del fallo técnico", restLabel = "2 min", notes = "Torso relativamente vertical y codos controlados. Es normal sentir algo de pecho."),
            routineExercise(dayId, ExerciseCatalog.extensionTricepsCuerda, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Codos estables."),
            routineExercise(dayId, ExerciseCatalog.extensionTricepsOverhead, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Buena opción para trabajar la cabeza larga del tríceps."),
        )
    )
}

private fun pullDay(routineId: String): RoutineDay {
    val dayId = "$routineId-miercoles"
    return RoutineDay(
        id = dayId,
        name = "Miércoles — Pull",
        dayOfWeek = DayOfWeek.WEDNESDAY,
        exercises = listOf(
            routineExercise(dayId, ExerciseCatalog.jalonAlPechoDominadas, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6–8", restLabel = "2–3 min", usesStraps = true, notes = "Usar straps si el agarre limita el trabajo de espalda."),
            routineExercise(dayId, ExerciseCatalog.remoConBarraOMancuerna, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–70% × 5–6", restLabel = "2–3 min", usesStraps = true, notes = "Sin impulso. Straps especialmente útiles en series pesadas."),
            routineExercise(dayId, ExerciseCatalog.remoChestSupported, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", usesStraps = true, notes = "Controlar el recorrido y evitar impulso."),
            routineExercise(dayId, ExerciseCatalog.facePulls, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "~15", restLabel = "60–75 s", notes = "Movimiento controlado."),
            routineExercise(dayId, ExerciseCatalog.curlBicepsBarra, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "90 s", notes = "Evitar balanceo."),
            routineExercise(dayId, ExerciseCatalog.curlMartillo, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Trabaja bíceps, braquial y braquiorradial."),
            routineExercise(dayId, ExerciseCatalog.wristCurls, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "60 s", notes = "Trabajo directo de antebrazo."),
        )
    )
}

private fun legsDay(routineId: String): RoutineDay {
    val dayId = "$routineId-jueves"
    return RoutineDay(
        id = dayId,
        name = "Jueves — Legs",
        dayOfWeek = DayOfWeek.THURSDAY,
        exercises = listOf(
            routineExercise(dayId, ExerciseCatalog.sentadillaHackSquatPrensa, approachSets = 2, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "50% × 6 → 70% × 4", restLabel = "2–3 min", notes = "Principal del día. Priorizar técnica y rango cómodo."),
            routineExercise(dayId, ExerciseCatalog.hipThrust, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6", restLabel = "2–3 min", notes = "Pausa breve arriba y control de la pelvis."),
            routineExercise(dayId, ExerciseCatalog.extensionCuadriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s", notes = "Controlar especialmente la bajada."),
            routineExercise(dayId, ExerciseCatalog.curlFemoral, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s", notes = "Recorrido controlado."),
            routineExercise(dayId, ExerciseCatalog.aductoresAbductores, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–75 s", notes = "Aductores = parte interna; abductores = parte externa. Se pueden alternar semanalmente."),
            routineExercise(dayId, ExerciseCatalog.pantorrillas, approachSets = 0, effectiveSets = 4, effectiveRepsLabel = "12–20", restLabel = "60–75 s", notes = "Recorrido amplio y controlado, con pausa arriba."),
        )
    )
}

private fun fullUpperDay(routineId: String): RoutineDay {
    val dayId = "$routineId-sabado"
    return RoutineDay(
        id = dayId,
        name = "Sábado — Full Upper",
        dayOfWeek = DayOfWeek.SATURDAY,
        exercises = listOf(
            routineExercise(dayId, ExerciseCatalog.pressInclinado, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6", restLabel = "2 min", notes = "Controlar el movimiento."),
            routineExercise(dayId, ExerciseCatalog.remoEnMaquinaOMancuerna, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", usesStraps = true, notes = "Straps opcionales según agarre."),
            routineExercise(dayId, ExerciseCatalog.jalonAlPecho, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min", usesStraps = true, notes = "Priorizar espalda sobre agarre."),
            routineExercise(dayId, ExerciseCatalog.elevacionesLaterales, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60 s", notes = "Volumen accesorio para deltoide lateral."),
            routineExercise(dayId, ExerciseCatalog.pressDeHombroEnMaquina, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "2 min", notes = "Más ligero que el martes."),
            routineExercise(dayId, ExerciseCatalog.facePulls, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "~15", restLabel = "60 s", notes = "Control y técnica."),
        )
    )
}

private fun armsLegsChestDay(routineId: String): RoutineDay {
    val dayId = "$routineId-domingo"
    return RoutineDay(
        id = dayId,
        name = "Domingo — Brazos + Pierna + Pecho",
        dayOfWeek = DayOfWeek.SUNDAY,
        exercises = listOf(
            routineExercise(dayId, ExerciseCatalog.prensaSentadillaLigera, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", notes = "Trabajo de pierna secundario."),
            routineExercise(dayId, ExerciseCatalog.curlFemoralExtensionCuadriceps, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Elegir según lo que quieras priorizar."),
            routineExercise(dayId, ExerciseCatalog.curlBicepsAlternado, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Controlar el movimiento."),
            routineExercise(dayId, ExerciseCatalog.curlMartillo, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Énfasis complementario en braquial y braquiorradial."),
            routineExercise(dayId, ExerciseCatalog.tricepsEnCuerda, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Codos estables."),
            routineExercise(dayId, ExerciseCatalog.extensionTricepsOverhead, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Recorrido cómodo."),
            routineExercise(dayId, ExerciseCatalog.pechoPressAperturas, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Trabajo adicional de pecho, sin necesidad de llevarlo al fallo."),
        )
    )
}

private fun defaultRoutine(): Routine {
    val routineId = "default"
    return Routine(
        id = routineId,
        name = "PPL + Upper/Accesorios",
        isDefault = true,
        days = listOf(
            pushDay(routineId),
            pullDay(routineId),
            legsDay(routineId),
            fullUpperDay(routineId),
            armsLegsChestDay(routineId),
        )
    )
}

private fun fullBodyRapidoRoutine(): Routine {
    val routineId = "full-body-rapido"
    val dayId = "$routineId-unico"
    return Routine(
        id = routineId,
        name = "Full Body Rápido",
        days = listOf(
            RoutineDay(
                id = dayId,
                name = "Único día",
                exercises = listOf(
                    routineExercise(dayId, ExerciseCatalog.sentadillaHackSquatPrensa, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min"),
                    routineExercise(dayId, ExerciseCatalog.pressDePecho, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min"),
                    routineExercise(dayId, ExerciseCatalog.remoConBarraOMancuerna, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min", usesStraps = true),
                )
            )
        )
    )
}

private fun soloPiernasRoutine(): Routine {
    val routineId = "solo-piernas"
    val dayId = "$routineId-unico"
    return Routine(
        id = routineId,
        name = "Solo Piernas",
        days = listOf(
            RoutineDay(
                id = dayId,
                name = "Único día",
                exercises = listOf(
                    routineExercise(dayId, ExerciseCatalog.sentadillaHackSquatPrensa, approachSets = 2, effectiveSets = 4, effectiveRepsLabel = "8–12", restLabel = "2–3 min"),
                    routineExercise(dayId, ExerciseCatalog.hipThrust, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", restLabel = "2–3 min"),
                    routineExercise(dayId, ExerciseCatalog.extensionCuadriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s"),
                )
            )
        )
    )
}

fun seedRoutines(): List<Routine> = listOf(
    defaultRoutine(),
    fullBodyRapidoRoutine(),
    soloPiernasRoutine(),
)
