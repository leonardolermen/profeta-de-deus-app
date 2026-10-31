package br.org.profetas.app.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class EventDateInputTest {

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    @Test fun strictRejectsFebruary30() {
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            parseEventDate("30/02/2026 10:00", saoPaulo)
        }

        assertEquals("Informe uma data válida (dd/MM/aaaa) e horário de 24 horas (HH:mm).", thrown.message)
    }

    @Test fun keepsDeviceZone() {
        val expected = OffsetDateTime.of(2026, 11, 7, 19, 30, 0, 0, ZoneOffset.ofHours(-3))

        assertEquals(expected, parseEventDate(" 07/11/2026 19:30 ", saoPaulo))
        assertEquals(ZoneOffset.ofHours(1), parseEventDate("01/07/2026 08:00", ZoneId.of("Europe/Lisbon")).offset)
    }

    @Test fun editingSavedEventRestoresBrazilianDateAndLocalTime() {
        assertEquals("07/11/2026 19:30", eventDateInput("2026-11-07T22:30:00Z", saoPaulo))
    }

    @Test fun rejectsAmericanOrderAndInvalidHours() {
        listOf("2026-11-07 19:30", "11/30/2026 19:30", "07/11/2026 24:00", "07/11/2026 19:60", "07/11/2026 7:30").forEach { input ->
            assertThrows(input, IllegalArgumentException::class.java) { parseEventDate(input, saoPaulo) }
        }
    }

    @Test fun leapDayAndMidnightArePreserved() {
        assertEquals("2028-02-29T00:00-03:00", parseEventDate("29/02/2028 00:00", saoPaulo).toString())
        assertThrows(IllegalArgumentException::class.java) { parseEventDate("29/02/2027 00:00", saoPaulo) }
    }

    @Test fun typingDigitsAddsBrazilianSeparators() {
        assertEquals("07/11/2026", formatEventDayInput("07112026"))
        assertEquals("07/11/2026", formatEventDayInput("07/11/2026"))
        assertEquals("07/1", formatEventDayInput("071"))
        assertEquals("07", formatEventDayInput("07/"))
    }

    @Test fun typingTimeAddsColonAndAllowsDeletingIt() {
        assertEquals("19:30", formatEventTimeInput("1930"))
        assertEquals("19:30", formatEventTimeInput("19:30"))
        assertEquals("19", formatEventTimeInput("19:"))
        assertEquals("", formatEventTimeInput(""))
    }
}
