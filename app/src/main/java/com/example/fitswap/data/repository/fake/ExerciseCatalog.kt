package com.example.fitswap.data.repository.fake

import com.example.fitswap.domain.model.Exercise

/**
 * Catálogo compartido de ejercicios, sembrado 1:1 desde `rutina_semanal_optimizada.md` (ver
 * [RoutineSeedData]). Vive en un archivo propio (no dentro de `RoutineSeedData`) porque M4
 * (sustitutos) necesita referenciar las mismas instancias de [Exercise] que la rutina por defecto,
 * y M6 (catálogo navegable) va a formalizar un `ExerciseRepository` sobre esta misma lista.
 */
object ExerciseCatalog {
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

    // Ejercicio base de calentamiento/cardio (docs/PRD.md, primera alternativa del catálogo en el
    // boceto de docs/DIAGRAMS.md) — no aparece en ningún día de la rutina por defecto.
    val caminarEnCinta = Exercise("caminar-en-cinta", "Caminar en cinta", "Cardio", "Cinta", tags = listOf("calentamiento"))

    val allExercises: List<Exercise> = listOf(
        caminarEnCinta,
        elevacionesLaterales, pressMilitarMaquina, pressDePecho, aperturasDePecho, fondosParaTriceps,
        extensionTricepsCuerda, extensionTricepsOverhead, jalonAlPechoDominadas, remoConBarraOMancuerna,
        remoChestSupported, facePulls, curlBicepsBarra, curlMartillo, wristCurls, sentadillaHackSquatPrensa,
        hipThrust, extensionCuadriceps, curlFemoral, aductoresAbductores, pantorrillas, pressInclinado,
        remoEnMaquinaOMancuerna, jalonAlPecho, pressDeHombroEnMaquina, prensaSentadillaLigera,
        curlFemoralExtensionCuadriceps, curlBicepsAlternado, tricepsEnCuerda, pechoPressAperturas,
    )
}
