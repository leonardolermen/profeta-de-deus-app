package br.org.profetas.app.admin

import br.org.profetas.app.event.eventInstant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

// STRICT: o padrão (SMART) aceitaria 30 de fevereiro e gravaria 28, um culto num dia que o admin não escolheu.
private val EVENT_DATE_INPUT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT)

// O admin digita a hora local do aparelho; o banco recebe com o deslocamento daquele dia nesse fuso.
fun parseEventDate(input: String, zone: ZoneId): OffsetDateTime {
    val local = try {
        LocalDateTime.parse(input.trim(), EVENT_DATE_INPUT)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Informe a data e hora no formato aaaa-MM-dd HH:mm.", e)
    }

    return local.atZone(zone).toOffsetDateTime()
}

// O caminho de volta, para o editor abrir com a data gravada no mesmo formato que o admin digita.
fun eventDateInput(date: String, zone: ZoneId): String =
    eventInstant(date)?.atZone(zone)?.format(EVENT_DATE_INPUT).orEmpty()
