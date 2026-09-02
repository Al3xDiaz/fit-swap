package com.example.fitswap.data.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Siempre resuelve a un martes, sin importar en qué fecha real corra el test — así "Empezar
 * entrenamiento" en la rutina por defecto abre siempre el mismo día (Push) por defecto, en vez de
 * depender del día de la semana real del dispositivo/CI que ejecuta el test.
 */
class FixedCurrentDateProvider @Inject constructor() : CurrentDateProvider {
    override fun today(): LocalDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
}
