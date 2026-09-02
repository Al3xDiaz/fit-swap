package com.example.fitswap.data.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Siempre devuelve una fecha con el [dayOfWeek] pedido, sin depender de cuándo corra el test. */
class FixedCurrentDateProvider(private val dayOfWeek: DayOfWeek) : CurrentDateProvider {
    override fun today(): LocalDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(dayOfWeek))
}
