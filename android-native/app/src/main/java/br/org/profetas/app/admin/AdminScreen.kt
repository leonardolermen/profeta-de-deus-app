package br.org.profetas.app.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure
import br.org.profetas.app.support.ui.ScreenColumn
import br.org.profetas.app.support.ui.SearchBox

private const val EVENT_DELETE_WARNING =
    "O evento e suas inscrições serão excluídos. Esta ação não pode ser desfeita."

@Composable
fun AdminScreen(viewModel: AdminViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var section by rememberSaveable { mutableStateOf(AdminSection.EVENTS) }
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<DeleteAction?>(null) }

    val items = sectionItems(section, state, query, viewModel, onNavigate)

    ScreenColumn {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = "Conteúdo da igreja",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Escolha a seção, edite ou exclua. Puxe a tela para baixo para buscar os cadastros recentes.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        SectionChips(
            selected = section,
            onSelect = { chosen ->
                section = chosen
                query = ""
            },
        )

        SearchBox(value = query, onChange = { text -> query = text }, placeholder = "Buscar nesta seção")

        section.newRecord?.let { route ->
            Button(onClick = { onNavigate(route) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(when (section) {
                    AdminSection.EVENTS -> "Criar novo evento"
                    AdminSection.WEEKLY_SERVICES -> "Adicionar culto semanal"
                    AdminSection.STUDIES -> "Adicionar estudo"
                    AdminSection.PHOTOS -> "Adicionar foto"
                    AdminSection.BIRTHDAYS -> "Adicionar aniversariante"
                    else -> "Novo cadastro"
                }, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        state.failure?.let { failure ->
            LoadFailure(message = failure, onRetry = viewModel::reload)
        }

        SectionList(
            items = items,
            isLoading = !state.hasLoaded && state.loading,
            onDelete = { action -> pendingDelete = action },
        )
    }

    pendingDelete?.let { action ->
        DeleteDialog(
            action = action,
            isSaving = state.saving,
            onDismiss = { pendingDelete = null },
        )
    }
}

// O único lugar que decide por seção o que aparece na lista e o que editar ou excluir faz.
private fun sectionItems(
    section: AdminSection,
    state: AdminState,
    query: String,
    viewModel: AdminViewModel,
    onNavigate: (Route) -> Unit,
): List<AdminItem> = when (section) {
    AdminSection.EVENTS -> state.events
        .filter { event -> matchesQuery(query, event.title) }
        .map { event ->
            AdminItem(
                title = event.title,
                subtitle = "${event.type.label} · ${displayDate(event.date)}",
                edit = EditAction("Editar") { onNavigate(Route.EventEditor(event.id)) },
                delete = DeleteAction("Excluir", EVENT_DELETE_WARNING) { viewModel.deleteEvent(event) },
            )
        }
    AdminSection.WEEKLY_SERVICES -> state.weeklyServices
        .filter { service -> matchesQuery(query, service.name, service.place) }
        .map { service ->
            AdminItem(
                title = service.name,
                subtitle = "${service.dayLabel} · ${service.times.joinToString(", ")}",
                edit = EditAction("Editar") { onNavigate(Route.WeeklyServiceEditor(service.id)) },
                delete = DeleteAction("Excluir", DEFAULT_DELETE_WARNING) { viewModel.deleteWeeklyService(service) },
            )
        }
    AdminSection.STUDIES -> state.studies
        .filter { study -> matchesQuery(query, study.title, study.category) }
        .map { study ->
            AdminItem(
                title = study.title,
                subtitle = study.category,
                edit = EditAction("Editar") { onNavigate(Route.StudyEditor(study.id)) },
                delete = DeleteAction("Excluir", DEFAULT_DELETE_WARNING) { viewModel.deleteStudy(study) },
            )
        }
    AdminSection.PHOTOS -> state.photos
        .filter { photo -> matchesQuery(query, photo.title, photo.album) }
        .map { photo ->
            AdminItem(
                title = photo.title,
                subtitle = photo.album,
                edit = EditAction("Editar") { onNavigate(Route.PhotoEditor(photo.id)) },
                delete = DeleteAction("Excluir", DEFAULT_DELETE_WARNING) { viewModel.deletePhoto(photo) },
            )
        }
    AdminSection.BIRTHDAYS -> state.birthdays
        .filter { birthday -> matchesQuery(query, birthday.name) }
        .map { birthday ->
            AdminItem(
                title = birthday.name,
                subtitle = "%02d/%02d".format(birthday.day, birthday.month),
                edit = EditAction("Editar") { onNavigate(Route.BirthdayEditor(birthday.id)) },
                delete = DeleteAction("Excluir", DEFAULT_DELETE_WARNING) { viewModel.deleteBirthday(birthday) },
            )
        }
    AdminSection.CHURCH -> listOf(
        AdminItem(
            title = state.church.name,
            subtitle = "Contatos, história e política de privacidade.",
            edit = EditAction("Editar informações") { onNavigate(Route.ChurchEditor) },
        ),
    )
    AdminSection.PRAYERS -> prayerItems(state.prayers, query, viewModel::deletePrayer)
    AdminSection.REGISTRATIONS ->
        registrationItems(state.registrations, state.events, query, viewModel::cancelRegistration)
}

@Composable
private fun SectionChips(selected: AdminSection, onSelect: (AdminSection) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AdminSection.entries.forEach { section ->
            FilterChip(
                selected = section == selected,
                onClick = { onSelect(section) },
                label = { Text(section.label) },
                colors = churchChipColors(),
            )
        }
    }
}

@Composable
private fun SectionList(items: List<AdminItem>, isLoading: Boolean, onDelete: (DeleteAction) -> Unit) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        return
    }

    if (items.isEmpty()) {
        EmptyState("Nenhum cadastro nesta seção.")

        return
    }

    Column(
        modifier = Modifier.padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { item ->
            AdminItemCard(item = item, onDelete = onDelete)
        }
    }
}

@Composable
private fun DeleteDialog(action: DeleteAction, isSaving: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir este registro?") },
        text = { Text(action.warning) },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    onDismiss()
                    action.perform()
                },
            ) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        },
    )
}
