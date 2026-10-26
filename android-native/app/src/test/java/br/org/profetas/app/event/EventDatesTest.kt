package br.org.profetas.app.event

import br.org.profetas.app.event.reminder.reminderDelay
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class EventDatesTest {
    private val now=Instant.parse("2026-10-21T12:00:00Z")
    @Test fun offsetsRepresentTheSameInstant() {
        assertEquals(eventInstant("2026-10-21T12:00:00Z"),eventInstant("2026-10-21T09:00:00-03:00"))
    }
    @Test fun pastAndMalformedEventsCannotSchedule() {
        assertNull(reminderDelay("2026-10-20T12:00:00Z",now))
        assertNull(reminderDelay("not a date",now))
        assertFalse(isFuture("2026-10-21T12:00:00Z",now))
    }

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    // Formato de toLocaleDateString('pt-BR', { day: '2-digit', month: 'long' }) em app/campanhas.tsx, no fuso
    // local: 01:00 UTC do dia 5 ainda é noite do dia 4 em São Paulo.
    @Test fun dayAndMonthLikeCampaigns() {
        assertEquals("22 de novembro", dayAndMonth("2026-11-22T19:30:00-03:00", saoPaulo))
        assertEquals("04 de janeiro", dayAndMonth("2027-01-05T01:00:00Z", saoPaulo))
    }

    // app/batismo.tsx acrescenta a hora: "12 de novembro às 21:13".
    @Test fun dayMonthAndTimeLikeBaptism() {
        assertEquals("12 de novembro às 21:13", dayMonthAndTime("2026-11-13T00:13:00Z", saoPaulo))
    }
}
