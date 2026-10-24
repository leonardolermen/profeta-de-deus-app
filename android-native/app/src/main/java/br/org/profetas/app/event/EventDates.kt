package br.org.profetas.app.event

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))

fun eventInstant(date: String): Instant? = runCatching { OffsetDateTime.parse(date).toInstant() }.getOrNull()

fun displayDate(date: String): String {
    val instant = eventInstant(date) ?: return date

    return instant.atZone(ZoneId.systemDefault()).format(DISPLAY_FORMAT)
}

fun isFuture(date: String, now: Instant = Instant.now()): Boolean = eventInstant(date)?.isAfter(now) == true
