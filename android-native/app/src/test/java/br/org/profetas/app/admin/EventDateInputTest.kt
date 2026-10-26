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
            parseEventDate("2026-02-30 10:00", saoPaulo)
        }

        assertEquals("Informe a data e hora no formato aaaa-MM-dd HH:mm.", thrown.message)
    }

    @Test fun keepsDeviceZone() {
        val expected = OffsetDateTime.of(2026, 11, 7, 19, 30, 0, 0, ZoneOffset.ofHours(-3))

        assertEquals(expected, parseEventDate(" 2026-11-07 19:30 ", saoPaulo))
        assertEquals(ZoneOffset.ofHours(1), parseEventDate("2026-07-01 08:00", ZoneId.of("Europe/Lisbon")).offset)
    }
}
