package br.org.profetas.app.home

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.PhotoEventCard
import br.org.profetas.app.support.ui.EmptyState

// Cada cartão ocupa 85% da largura e fica centralizado, com o vizinho aparecendo na borda (carousel de
// app/(tabs)/index.tsx).
private const val CARD_WIDTH_FRACTION = 0.85f

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

    BoxWithConstraints {
        val cardWidth = maxWidth * CARD_WIDTH_FRACTION
        val sidePadding = (maxWidth - cardWidth) / 2

        val listState = rememberLazyListState()

        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(horizontal = sidePadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(events, key = { event -> event.id }) { event ->
                PhotoEventCard(
                    event = event,
                    storageBaseUrl = storageBaseUrl,
                    onClick = { onOpen(event) },
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}
