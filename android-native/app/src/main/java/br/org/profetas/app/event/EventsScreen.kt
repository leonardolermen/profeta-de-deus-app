package br.org.profetas.app.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.weekly.WeeklyServicesSection
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure
import br.org.profetas.app.support.ui.SearchBox

// Aba Eventos, a partir de app/(tabs)/eventos.tsx: as medidas (margens, fontes, raios) são as do StyleSheet de lá.
// O Expo não filtrava a busca nem tinha tipos e eventos passados; os filtros vêm da tela Kotlin anterior.
@Composable
fun EventsScreen(viewModel: EventsViewModel, storageBaseUrl: String, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<EventType?>(null) }
    var includePast by rememberSaveable { mutableStateOf(false) }

    val events = viewModel.filtered(query, type, includePast)

    // A barra de status já vem no padding do Scaffold; 24 + a barra fica perto dos 60 de paddingTop do Expo.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 60.dp),
    ) {
        Header(onNavigate)

        SearchBox(
            value = query,
            onChange = { text -> query = text },
            placeholder = "Buscar por culto ou retiro...",
            modifier = Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp)),
        )

        TypeChips(selected = type, onSelect = { selected -> type = selected })

        IncludePastRow(checked = includePast, onChange = { checked -> includePast = checked })

        state.failure?.let { failure ->
            LoadFailure(
                message = failure,
                onRetry = viewModel::reload,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        SectionHeading(title = "Cultos Semanais", modifier = Modifier.padding(top = 30.dp))

        WeeklyServicesSection(services = state.weeklyServices, isLoading = state.loading)

        SectionHeading(title = "Eventos Especiais", modifier = Modifier.padding(top = 30.dp))

        SpecialEvents(
            events = events,
            isLoading = state.loading,
            hasAnyEvent = state.events.isNotEmpty(),
            storageBaseUrl = storageBaseUrl,
            onOpen = { event -> onNavigate(Route.EventDetail(event.id)) },
        )
    }
}

@Composable
private fun Header(onNavigate: (Route) -> Unit) {
    Text(
        text = "FIQUE POR DENTRO",
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp),
    )

    Text(
        text = "Agenda e Eventos",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Row(
        modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionButton(
            emoji = "📸",
            label = "Fotos",
            onClick = { onNavigate(Route.Gallery) },
            modifier = Modifier.weight(1f),
        )

        ActionButton(
            emoji = "💧",
            label = "Batismo",
            onClick = { onNavigate(Route.Baptism) },
            modifier = Modifier.weight(1f),
        )
    }
}

// actionBtn/actionBtnIcon/actionBtnText de app/(tabs)/eventos.tsx.
@Composable
private fun ActionButton(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TypeChips(selected: EventType?, onSelect: (EventType?) -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Sem isso o chip marcado sai no lilás padrão do Material, fora da paleta da igreja.
        val colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
        )

        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("Todos") },
            colors = colors,
        )

        EventType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.label) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun IncludePastRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)

        Text(
            text = "Incluir eventos anteriores",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// sectionHeaderRow/sectionTitle de app/(tabs)/eventos.tsx.
@Composable
private fun SectionHeading(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 16.dp),
    )
}

// O Expo mostrava os eventos num carrossel horizontal; aqui a lista é filtrável, então vai na vertical, com o
// mesmo cartão de foto da Início.
@Composable
private fun SpecialEvents(
    events: List<Event>,
    isLoading: Boolean,
    hasAnyEvent: Boolean,
    storageBaseUrl: String,
    onOpen: (Event) -> Unit,
) {
    if (events.isEmpty()) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            EmptyState(if (hasAnyEvent) "Nenhum evento encontrado." else "Nenhum evento especial no momento.")
        }

        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        events.forEach { event ->
            PhotoEventCard(
                event = event,
                storageBaseUrl = storageBaseUrl,
                onClick = { onOpen(event) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
