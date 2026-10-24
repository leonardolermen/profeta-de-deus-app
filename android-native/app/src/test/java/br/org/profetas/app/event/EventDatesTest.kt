package br.org.profetas.app.event

import br.org.profetas.app.event.reminder.reminderDelay
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

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
}
