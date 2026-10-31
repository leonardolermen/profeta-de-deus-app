package br.org.profetas.app.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.support.ui.*

@Composable
fun EventRegistrationsScreen(eventId: String, viewModel: AdminViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(eventId) { viewModel.reload() }
    val event = state.events.find { it.id == eventId }
    val registrations = state.registrations.filter { it.eventId == eventId }
    val filtered = registrations.filter { matchesQuery(query, it.name, it.phone) }
    ScreenColumn(Modifier.padding(top = 20.dp, bottom = 24.dp)) {
        Text(event?.title ?: "Inscritos no evento", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        event?.let { Text(displayDate(it.date), fontSize = 18.sp) }
        Text("Visível somente para administradores. Nomes e contatos informados na inscrição.", fontSize = 17.sp)
        OutlinedButton(onClick = viewModel::reload, enabled = !state.loading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Atualizar inscritos", fontSize = 18.sp) }
        if (state.loading) CircularProgressIndicator()
        state.failure?.let { LoadFailure(it, viewModel::reload) }
        if (state.hasLoaded && state.failure == null) {
            Text("${registrations.size} inscrição(ões)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            SearchBox(query, { query = it }, placeholder = "Buscar nome ou telefone")
            if (filtered.isEmpty()) EmptyState(if (registrations.isEmpty()) "Ninguém se inscreveu neste evento ainda." else "Nenhuma inscrição encontrada.")
            filtered.forEach { registration ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(registration.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Telefone: ${registration.phone.ifBlank { "Não informado" }}", fontSize = 19.sp)
                    }
                }
            }
        }
    }
}
