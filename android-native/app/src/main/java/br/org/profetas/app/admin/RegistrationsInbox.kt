package br.org.profetas.app.admin

import br.org.profetas.app.event.Event
import br.org.profetas.app.event.registration.Registration

// Inscrições de todos os membros (a RLS só devolve todas ao admin), com o título do evento ao lado do nome.
fun registrationItems(
    registrations: List<Registration>,
    events: List<Event>,
    query: String,
    onCancel: (Registration) -> Unit,
): List<AdminItem> {
    val titles = events.associate { event -> event.id to event.title }

    return registrations
        .filter { registration -> matchesQuery(query, registration.name, titles[registration.eventId]) }
        .map { registration ->
            AdminItem(
                title = registration.name,
                subtitle = titles[registration.eventId] ?: "Evento",
                body = registration.phone,
                delete = DeleteAction("Cancelar inscrição", DEFAULT_DELETE_WARNING) { onCancel(registration) },
            )
        }
}
