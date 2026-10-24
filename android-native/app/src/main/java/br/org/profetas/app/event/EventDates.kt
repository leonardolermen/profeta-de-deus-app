package br.org.profetas.app.event

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BRAZILIAN_PORTUGUESE = Locale.forLanguageTag("pt-BR")

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy 'às' HH:mm", BRAZILIAN_PORTUGUESE)

private val DAY_AND_MONTH_FORMAT = DateTimeFormatter.ofPattern("dd 'de' MMMM", BRAZILIAN_PORTUGUESE)

private val DAY_MONTH_AND_TIME_FORMAT = DateTimeFormatter.ofPattern("dd 'de' MMMM 'às' HH:mm", BRAZILIAN_PORTUGUESE)

fun eventInstant(date: String): Instant? = runCatching { OffsetDateTime.parse(date).toInstant() }.getOrNull()

fun displayDate(date: String): String {
    val instant = eventInstant(date) ?: return date

    return instant.atZone(ZoneId.systemDefault()).format(DISPLAY_FORMAT)
}

fun isFuture(date: String, now: Instant = Instant.now()): Boolean = eventInstant(date)?.isAfter(now) == true

fun dayAndMonth(date: String, zone: ZoneId = ZoneId.systemDefault()): String = format(date, zone, DAY_AND_MONTH_FORMAT)

fun dayMonthAndTime(date: String, zone: ZoneId = ZoneId.systemDefault()): String =
    format(date, zone, DAY_MONTH_AND_TIME_FORMAT)

private fun format(date: String, zone: ZoneId, formatter: DateTimeFormatter): String {
    val instant = eventInstant(date) ?: return date

    return instant.atZone(zone).format(formatter)
}
