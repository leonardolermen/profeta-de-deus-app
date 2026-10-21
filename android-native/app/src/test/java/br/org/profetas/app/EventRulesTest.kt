package br.org.profetas.app

import br.org.profetas.app.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class EventRulesTest {
    private val now=Instant.parse("2026-10-21T12:00:00Z")
    @Test fun offsetsRepresentTheSameInstant() {
        assertEquals(eventInstant("2026-10-21T12:00:00Z"),eventInstant("2026-10-21T09:00:00-03:00"))
    }
    @Test fun pastAndMalformedEventsCannotSchedule() {
        assertNull(reminderDelay("2026-10-20T12:00:00Z",now))
        assertNull(reminderDelay("not a date",now))
        assertFalse(isFuture("2026-10-21T12:00:00Z",now))
    }
    @Test fun reminderUsesEventInstantRatherThanWallClock() {
        assertEquals(3_600_000L,reminderDelay("2026-10-21T11:00:00-03:00",now))
        assertEquals(0L,reminderDelay("2026-10-21T12:30:00Z",now))
    }
    @Test fun photosRequireAnActualHttpsHost() {
        assertTrue(validHttps("https://example.org/photo.jpg"))
        assertFalse(validHttps("http://example.org/photo.jpg"))
        assertFalse(validHttps("https:///photo.jpg"))
        assertFalse(validHttps("javascript:alert(1)"))
        assertFalse(validHttps("https://user:password@example.org/image.jpg"))
    }
}
