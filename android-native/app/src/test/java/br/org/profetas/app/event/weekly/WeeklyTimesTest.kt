package br.org.profetas.app.event.weekly

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WeeklyTimesTest {

    @Test fun parsesSortsAndDedupes() {
        assertEquals(listOf("08:00", "15:00", "18:00"), parseTimes("18:00, 08:00 15:00,08:00"))

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            parseTimes("25:00")
        }

        assertEquals("Horário inválido: 25:00", thrown.message)
    }
}
