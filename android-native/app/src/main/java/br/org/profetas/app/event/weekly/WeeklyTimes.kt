package br.org.profetas.app.event.weekly

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)

private val SEPARATORS = Regex("""[,\s]+""")

// Horários de um culto semanal como o admin digita ("08:00, 15:00 18:00"): em ordem e sem repetidos, porque a aba
// Eventos mostra a lista exatamente como foi gravada.
fun parseTimes(input: String): List<String> {
    val items = input.split(SEPARATORS).filter { item -> item.isNotEmpty() }

    val times = items.map { item ->
        try {
            LocalTime.parse(item, TIME_FORMAT)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Horário inválido: $item", e)
        }
    }

    return times.distinct().sorted().map { time -> time.format(TIME_FORMAT) }
}
