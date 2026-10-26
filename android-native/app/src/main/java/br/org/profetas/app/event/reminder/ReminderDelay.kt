package br.org.profetas.app.event.reminder

import br.org.profetas.app.event.eventInstant
import java.time.Duration
import java.time.Instant

fun reminderDelay(date: String, now: Instant = Instant.now()): Long? {
    val instant = eventInstant(date) ?: return null
    if (!instant.isAfter(now)) {
        return null
    }

    return Duration.between(now, instant.minusSeconds(3600)).toMillis().coerceAtLeast(0)
}
