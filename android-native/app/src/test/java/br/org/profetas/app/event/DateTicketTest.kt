package br.org.profetas.app.event

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class DateTicketTest {

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    // O Expo calculava o ticket em UTC por engano (ver app/(tabs)/eventos.tsx); aqui corrigimos de
    // propósito para o fuso do aparelho, por isso o teste cruza a meia-noite.
    @Test fun usesDeviceZoneNotUtc() {
        val ticket = ticketDate(Instant.parse("2026-06-25T01:30:00Z"), saoPaulo)

        assertEquals(TicketDate("24", "JUN", "22:30"), ticket)
    }

    @Test fun padsDay() {
        val instant = OffsetDateTime.parse("2026-05-03T12:00:00-03:00").toInstant()

        val ticket = ticketDate(instant, saoPaulo)

        assertEquals("03", ticket.day)
        assertEquals("MAI", ticket.month)
    }
}
