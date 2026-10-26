package br.org.profetas.app.event.registration

data class Registration(
    val id: String,
    val eventId: String,
    val name: String,
    val phone: String,
    val userId: String,
)
