package br.org.profetas.app.event.reminder

import br.org.profetas.app.event.Event

// Lembretes que o membro ativou. Interface para o EventsViewModel ser testado sem WorkManager nem SharedPreferences.
interface EventReminders {

    fun schedule(event: Event)

    fun cancel(eventId: String)

    fun enabledIds(): Set<String>
}
