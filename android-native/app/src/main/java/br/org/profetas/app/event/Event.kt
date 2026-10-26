package br.org.profetas.app.event

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val type: EventType,
    val imagePath: String?,
)

data class EventDraft(
    val title: String,
    val description: String,
    val date: String,
    val type: EventType,
    val imagePath: String?,
)
