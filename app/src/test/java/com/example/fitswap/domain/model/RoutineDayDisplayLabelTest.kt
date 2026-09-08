package com.example.fitswap.domain.model

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineDayDisplayLabelTest {

    private fun day(name: String, dayOfWeek: DayOfWeek? = null) =
        RoutineDay(id = "d1", name = name, exercises = emptyList(), dayOfWeek = dayOfWeek)

    @Test
    fun `nombre distinto al dia de la semana se combina con guion`() {
        val label = day(name = "Push", dayOfWeek = DayOfWeek.TUESDAY).displayLabel()

        assertEquals("Martes — Push", label)
    }

    @Test
    fun `nombre igual al label del dia no se repite`() {
        val label = day(name = "Martes", dayOfWeek = DayOfWeek.TUESDAY).displayLabel()

        assertEquals("Martes", label)
    }

    @Test
    fun `nombre en blanco no se repite, solo se muestra el dia`() {
        val label = day(name = "", dayOfWeek = DayOfWeek.TUESDAY).displayLabel()

        assertEquals("Martes", label)
    }

    @Test
    fun `sin dia de la semana se muestra solo el nombre`() {
        val label = day(name = "Día único", dayOfWeek = null).displayLabel()

        assertEquals("Día único", label)
    }
}
