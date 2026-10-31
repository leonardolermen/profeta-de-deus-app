package br.org.profetas.app.admin

import br.org.profetas.app.event.eventInstant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

// STRICT: o padrão (SMART) aceitaria 30 de fevereiro e gravaria 28, um culto num dia que o admin não escolheu.
private val EVENT_DATE_INPUT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT)

fun formatEventDayInput(input: String): String {
    val digits = input.filter(Char::isDigit).take(8)
    return when {
        digits.length > 4 -> "${digits.take(2)}/${digits.substring(2, 4)}/${digits.drop(4)}"
        digits.length > 2 -> "${digits.take(2)}/${digits.drop(2)}"
        else -> digits
    }
}

fun formatEventTimeInput(input: String): String {
    val digits = input.filter(Char::isDigit).take(4)
    return if (digits.length > 2) "${digits.take(2)}:${digits.drop(2)}" else digits
}

// O admin digita a hora local do aparelho; o banco recebe com o deslocamento daquele dia nesse fuso.
fun parseEventDate(input: String, zone: ZoneId): OffsetDateTime {
    val local = try {
        LocalDateTime.parse(input.trim(), EVENT_DATE_INPUT)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Informe uma data válida (dd/MM/aaaa) e horário de 24 horas (HH:mm).", e)
    }

    return local.atZone(zone).toOffsetDateTime()
}

// O caminho de volta, para o editor abrir com a data gravada no mesmo formato que o admin digita.
fun eventDateInput(date: String, zone: ZoneId): String =
    eventInstant(date)?.atZone(zone)?.format(EVENT_DATE_INPUT).orEmpty()
