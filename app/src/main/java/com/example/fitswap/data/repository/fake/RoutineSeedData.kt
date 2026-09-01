package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.Exercise
import com.example.fitswap.domain.model.Routine
import com.example.fitswap.domain.model.RoutineDay
import com.example.fitswap.domain.model.RoutineExercise

private object Exercises {
    val elevacionesLaterales = Exercise("elevaciones-laterales", "Elevaciones laterales", "Hombros", "Mancuerna")
    val pressMilitarMaquina = Exercise("press-militar-maquina", "Press militar en máquina", "Hombros", "Máquina")
    val pressDePecho = Exercise("press-de-pecho", "Press de pecho", "Pecho", "Máquina/Mancuerna")
    val aperturasDePecho = Exercise("aperturas-de-pecho", "Aperturas de pecho", "Pecho", "Máquina/Mancuerna")
    val fondosParaTriceps = Exercise("fondos-triceps", "Fondos para tríceps", "Tríceps", "Peso corporal")
    val extensionTricepsCuerda = Exercise("extension-triceps-cuerda", "Extensión de tríceps en cuerda", "Tríceps", "Cable")
    val extensionTricepsOverhead = Exercise("extension-triceps-overhead", "Extensión de tríceps overhead", "Tríceps", "Cable/Mancuerna")
    val jalonAlPechoDominadas = Exercise("jalon-al-pecho-dominadas", "Jalón al pecho / dominadas", "Espalda", "Cable/Peso corporal")
    val remoConBarraOMancuerna = Exercise("remo-barra-mancuerna", "Remo con barra o mancuerna", "Espalda", "Barra/Mancuerna")
    val remoChestSupported = Exercise("remo-chest-supported", "Remo chest-supported", "Espalda", "Máquina")
    val facePulls = Exercise("face-pulls", "Face pulls", "Espalda (deltoide posterior)", "Cable")
    val curlBicepsBarra = Exercise("curl-biceps-barra", "Curl de bíceps con barra", "Bíceps", "Barra")
    val curlMartillo = Exercise("curl-martillo", "Curl martillo", "Bíceps", "Mancuerna")
    val wristCurls = Exercise("wrist-curls", "Wrist curls / extensiones de muñeca", "Antebrazo", "Mancuerna")
    val sentadillaHackSquatPrensa = Exercise("sentadilla-hack-prensa", "Sentadilla / hack squat / prensa", "Piernas", "Barra/Máquina")
    val hipThrust = Exercise("hip-thrust", "Hip thrust", "Glúteos", "Barra/Máquina")
    val extensionCuadriceps = Exercise("extension-cuadriceps", "Extensión de cuádriceps", "Cuádriceps", "Máquina")
    val curlFemoral = Exercise("curl-femoral", "Curl femoral", "Isquiotibiales", "Máquina")
    val aductoresAbductores = Exercise("aductores-abductores", "Aductores / abductores", "Piernas", "Máquina")
    val pantorrillas = Exercise("pantorrillas", "Pantorrillas", "Pantorrillas", "Máquina")
    val pressInclinado = Exercise("press-inclinado", "Press inclinado", "Pecho", "Barra/Mancuerna")
    val remoEnMaquinaOMancuerna = Exercise("remo-maquina-mancuerna", "Remo en máquina / mancuerna", "Espalda", "Máquina/Mancuerna")
    val jalonAlPecho = Exercise("jalon-al-pecho", "Jalón al pecho", "Espalda", "Cable")
    val pressDeHombroEnMaquina = Exercise("press-hombro-maquina", "Press de hombro en máquina", "Hombros", "Máquina")
    val prensaSentadillaLigera = Exercise("prensa-sentadilla-ligera", "Prensa / sentadilla ligera", "Piernas", "Máquina")
    val curlFemoralExtensionCuadriceps = Exercise("curl-femoral-extension-cuadriceps", "Curl femoral / extensión de cuádriceps", "Piernas", "Máquina")
    val curlBicepsAlternado = Exercise("curl-biceps-alternado", "Curl de bíceps alternado", "Bíceps", "Mancuerna")
    val tricepsEnCuerda = Exercise("triceps-en-cuerda", "Tríceps en cuerda", "Tríceps", "Cable")
    val pechoPressAperturas = Exercise("pecho-press-aperturas", "Pecho: press / aperturas", "Pecho", "Máquina/Mancuerna")
}

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
        exercises = listOf(
            routineExercise(dayId, Exercises.elevacionesLaterales, approachSets = 0, effectiveSets = 4, effectiveRepsLabel = "12–15", restLabel = "60–75 s", notes = "Deltoide lateral primero para priorizarlo. Brazo ligeramente hacia delante."),
            routineExercise(dayId, Exercises.pressMilitarMaquina, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60% × 6–8", restLabel = "2–3 min", notes = "Ajustar asiento para que los agarres queden aproximadamente a nivel de la barbilla; codos ~45°."),
            routineExercise(dayId, Exercises.pressDePecho, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6–8", restLabel = "2–3 min", notes = "Máquina o mancuernas. Control y escápulas estables."),
            routineExercise(dayId, Exercises.aperturasDePecho, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Recorrido cómodo y controlado."),
            routineExercise(dayId, Exercises.fondosParaTriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "Cerca del fallo técnico", restLabel = "2 min", notes = "Torso relativamente vertical y codos controlados. Es normal sentir algo de pecho."),
            routineExercise(dayId, Exercises.extensionTricepsCuerda, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Codos estables."),
            routineExercise(dayId, Exercises.extensionTricepsOverhead, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "60–90 s", notes = "Buena opción para trabajar la cabeza larga del tríceps."),
        )
    )
}

private fun pullDay(routineId: String): RoutineDay {
    val dayId = "$routineId-miercoles"
    return RoutineDay(
        id = dayId,
        name = "Miércoles — Pull",
        exercises = listOf(
            routineExercise(dayId, Exercises.jalonAlPechoDominadas, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6–8", restLabel = "2–3 min", usesStraps = true, notes = "Usar straps si el agarre limita el trabajo de espalda."),
            routineExercise(dayId, Exercises.remoConBarraOMancuerna, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–70% × 5–6", restLabel = "2–3 min", usesStraps = true, notes = "Sin impulso. Straps especialmente útiles en series pesadas."),
            routineExercise(dayId, Exercises.remoChestSupported, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", usesStraps = true, notes = "Controlar el recorrido y evitar impulso."),
            routineExercise(dayId, Exercises.facePulls, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "~15", restLabel = "60–75 s", notes = "Movimiento controlado."),
            routineExercise(dayId, Exercises.curlBicepsBarra, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "90 s", notes = "Evitar balanceo."),
            routineExercise(dayId, Exercises.curlMartillo, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Trabaja bíceps, braquial y braquiorradial."),
            routineExercise(dayId, Exercises.wristCurls, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "60 s", notes = "Trabajo directo de antebrazo."),
        )
    )
}

private fun legsDay(routineId: String): RoutineDay {
    val dayId = "$routineId-jueves"
    return RoutineDay(
        id = dayId,
        name = "Jueves — Legs",
        exercises = listOf(
            routineExercise(dayId, Exercises.sentadillaHackSquatPrensa, approachSets = 2, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "50% × 6 → 70% × 4", restLabel = "2–3 min", notes = "Principal del día. Priorizar técnica y rango cómodo."),
            routineExercise(dayId, Exercises.hipThrust, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6", restLabel = "2–3 min", notes = "Pausa breve arriba y control de la pelvis."),
            routineExercise(dayId, Exercises.extensionCuadriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s", notes = "Controlar especialmente la bajada."),
            routineExercise(dayId, Exercises.curlFemoral, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s", notes = "Recorrido controlado."),
            routineExercise(dayId, Exercises.aductoresAbductores, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60–75 s", notes = "Aductores = parte interna; abductores = parte externa. Se pueden alternar semanalmente."),
            routineExercise(dayId, Exercises.pantorrillas, approachSets = 0, effectiveSets = 4, effectiveRepsLabel = "12–20", restLabel = "60–75 s", notes = "Recorrido amplio y controlado, con pausa arriba."),
        )
    )
}

private fun fullUpperDay(routineId: String): RoutineDay {
    val dayId = "$routineId-sabado"
    return RoutineDay(
        id = dayId,
        name = "Sábado — Full Upper",
        exercises = listOf(
            routineExercise(dayId, Exercises.pressInclinado, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", approachGuideline = "60–65% × 6", restLabel = "2 min", notes = "Controlar el movimiento."),
            routineExercise(dayId, Exercises.remoEnMaquinaOMancuerna, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", usesStraps = true, notes = "Straps opcionales según agarre."),
            routineExercise(dayId, Exercises.jalonAlPecho, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min", usesStraps = true, notes = "Priorizar espalda sobre agarre."),
            routineExercise(dayId, Exercises.elevacionesLaterales, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "60 s", notes = "Volumen accesorio para deltoide lateral."),
            routineExercise(dayId, Exercises.pressDeHombroEnMaquina, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "2 min", notes = "Más ligero que el martes."),
            routineExercise(dayId, Exercises.facePulls, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "~15", restLabel = "60 s", notes = "Control y técnica."),
        )
    )
}

private fun armsLegsChestDay(routineId: String): RoutineDay {
    val dayId = "$routineId-domingo"
    return RoutineDay(
        id = dayId,
        name = "Domingo — Brazos + Pierna + Pecho",
        exercises = listOf(
            routineExercise(dayId, Exercises.prensaSentadillaLigera, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "2 min", notes = "Trabajo de pierna secundario."),
            routineExercise(dayId, Exercises.curlFemoralExtensionCuadriceps, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Elegir según lo que quieras priorizar."),
            routineExercise(dayId, Exercises.curlBicepsAlternado, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Controlar el movimiento."),
            routineExercise(dayId, Exercises.curlMartillo, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Énfasis complementario en braquial y braquiorradial."),
            routineExercise(dayId, Exercises.tricepsEnCuerda, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Codos estables."),
            routineExercise(dayId, Exercises.extensionTricepsOverhead, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "12–15", restLabel = "75 s", notes = "Recorrido cómodo."),
            routineExercise(dayId, Exercises.pechoPressAperturas, approachSets = 0, effectiveSets = 2, effectiveRepsLabel = "10–12", restLabel = "90 s", notes = "Trabajo adicional de pecho, sin necesidad de llevarlo al fallo."),
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
                    routineExercise(dayId, Exercises.sentadillaHackSquatPrensa, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min"),
                    routineExercise(dayId, Exercises.pressDePecho, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min"),
                    routineExercise(dayId, Exercises.remoConBarraOMancuerna, approachSets = 1, effectiveSets = 3, effectiveRepsLabel = "8–12", restLabel = "2 min", usesStraps = true),
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
                    routineExercise(dayId, Exercises.sentadillaHackSquatPrensa, approachSets = 2, effectiveSets = 4, effectiveRepsLabel = "8–12", restLabel = "2–3 min"),
                    routineExercise(dayId, Exercises.hipThrust, approachSets = 1, effectiveSets = 4, effectiveRepsLabel = "8–12", restLabel = "2–3 min"),
                    routineExercise(dayId, Exercises.extensionCuadriceps, approachSets = 0, effectiveSets = 3, effectiveRepsLabel = "12–15", restLabel = "75–90 s"),
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
