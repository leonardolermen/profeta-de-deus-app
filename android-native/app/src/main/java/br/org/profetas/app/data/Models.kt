package br.org.profetas.app.data

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Event(val id: String, val title: String, val description: String, val date: String, val type: String)
data class Study(val id: String, val title: String, val description: String, val category: String, val content: String)
data class Birthday(val id: String, val name: String, val day: Int, val month: Int, val consent: Boolean)
data class Photo(val id: String, val title: String, val url: String, val album: String)
data class Church(val name: String = "Igreja Profetas de Deus", val phone: String = "", val whatsapp: String = "", val email: String = "", val address: String = "", val history: String = "", val privacy: String = "")
data class Profile(val id: String, val name: String, val role: String)
data class Prayer(val id: String, val name: String, val text: String, val date: String)
data class Registration(val id: String, val eventId: String, val name: String, val phone: String, val userId: String)
data class Book(val name: String, val abbrev: String, val chapters: Int)
data class Verse(val number: Int, val text: String)
data class Chapter(val book: String, val number: Int, val verses: List<Verse>)
data class Session(val access: String, val refresh: String, val userId: String, val email: String)

fun eventInstant(date: String): Instant? = runCatching { OffsetDateTime.parse(date).toInstant() }.getOrNull()
fun displayDate(date: String): String = eventInstant(date)?.atZone(ZoneId.systemDefault())
    ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))) ?: date
fun isFuture(date: String, now: Instant = Instant.now()): Boolean = eventInstant(date)?.isAfter(now) == true
fun reminderDelay(date: String, now: Instant = Instant.now()): Long? {
    val instant = eventInstant(date) ?: return null
    if (!instant.isAfter(now)) return null
    return Duration.between(now, instant.minusSeconds(3600)).toMillis().coerceAtLeast(0)
}
