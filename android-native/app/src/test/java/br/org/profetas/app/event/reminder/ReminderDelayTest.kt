package br.org.profetas.app.event.reminder

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ReminderDelayTest {
    private val now=Instant.parse("2026-10-21T12:00:00Z")
    @Test fun reminderUsesEventInstantRatherThanWallClock() {
        assertEquals(3_600_000L,reminderDelay("2026-10-21T11:00:00-03:00",now))
        assertEquals(0L,reminderDelay("2026-10-21T12:30:00Z",now))
    }
}
