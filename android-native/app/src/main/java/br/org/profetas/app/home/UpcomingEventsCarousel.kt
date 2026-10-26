package br.org.profetas.app.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventCarousel
import br.org.profetas.app.support.ui.EmptyState

@Composable
fun UpcomingEventsCarousel(
    events: List<Event>,
    isLoading: Boolean,
    storageBaseUrl: String,
    onOpen: (Event) -> Unit,
) {
    if (events.isEmpty()) {
        if (!isLoading) {
            EmptyState(
                text = "Nenhum encontro publicado para os próximos dias.",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        return
    }

    EventCarousel(events = events, storageBaseUrl = storageBaseUrl, onOpen = onOpen)
}
