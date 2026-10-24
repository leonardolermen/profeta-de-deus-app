package br.org.profetas.app.data

data class Event(val id: String, val title: String, val description: String, val date: String, val type: String)
data class Study(val id: String, val title: String, val description: String, val category: String, val content: String)
data class Birthday(val id: String, val name: String, val day: Int, val month: Int, val consent: Boolean)
data class Photo(val id: String, val title: String, val url: String, val album: String)
data class Church(val name: String = "Igreja Profetas de Deus", val phone: String = "", val whatsapp: String = "", val email: String = "", val address: String = "", val history: String = "", val privacy: String = "")
data class Prayer(val id: String, val name: String, val text: String, val date: String)
data class Registration(val id: String, val eventId: String, val name: String, val phone: String, val userId: String)
data class Book(val name: String, val abbrev: String, val chapters: Int)
data class Verse(val number: Int, val text: String)
data class Chapter(val book: String, val number: Int, val verses: List<Verse>)
