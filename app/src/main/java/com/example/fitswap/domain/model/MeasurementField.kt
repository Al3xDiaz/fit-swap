package com.example.fitswap.domain.model

/**
 * Las 11 circunferencias opcionales que se pueden registrar en una medición corporal (el peso
 * queda afuera a propósito — es el único campo obligatorio, siempre visible). Compartido entre el
 * selector de campos del stepper de "Nueva medición" y la guía "Cómo medir", para no mantener la
 * misma lista de 11 medidas duplicada en dos lugares.
 */
enum class MeasurementField(val label: String, val tag: String) {
    NECK("Cuello", "neck"),
    WAIST("Cintura", "waist"),
    HIP("Cadera", "hip"),
    CHEST("Pecho", "chest"),
    ARM("Brazo", "arm"),
    LEG("Pierna", "leg"),
    CALF("Pantorrilla", "calf"),
    GLUTE("Glúteo", "glute"),
    FOREARM("Antebrazo", "forearm"),
    SHOULDER("Hombro", "shoulder"),
    WRIST("Muñeca", "wrist"),
}
