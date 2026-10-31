package br.org.profetas.app.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.admin.EditorColumn
import br.org.profetas.app.admin.EditorRecord
import br.org.profetas.app.admin.FieldHint
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.intent.openLink
import br.org.profetas.app.support.ui.*

@Composable
fun RecommendationsScreen(viewModel: RecommendationsViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Recommendation?>(null) }
    LaunchedEffect(Unit) { viewModel.reload() }
    ScreenColumn(Modifier.padding(top = 20.dp, bottom = 24.dp)) {
        Text("Indicações", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Aplicativos, produtos e serviços indicados pela igreja.", fontSize = 18.sp)
        if (state.canManage) {
            Button(onClick = { onNavigate(Route.RecommendationEditor(Route.NEW_RECORD)) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Adicionar indicação", fontSize = 18.sp)
            }
        }
        SearchBox(query, { query = it }, placeholder = "Buscar indicação")
        OutlinedButton(onClick = viewModel::reload, enabled = !state.loading) { Text("Atualizar indicações") }
        if (state.loading) CircularProgressIndicator()
        state.failure?.let { LoadFailure(it, viewModel::reload) }
        val items = state.items.filter { listOf(it.title, it.description, it.contact).any { text -> text.contains(query.trim(), ignoreCase = true) } }
        if (!state.loading && state.failure == null && items.isEmpty()) EmptyState("Nenhuma indicação encontrada.")
        items.forEach { item ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(item.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(item.description, fontSize = 19.sp, lineHeight = 28.sp)
                    if (item.contact.isNotBlank()) Text("Contato: ${item.contact}", fontSize = 18.sp)
                    if (safeRecommendationLink(item.link)) OutlinedButton(onClick = { openLink(context, item.link) }) { Text("Abrir link") }
                    if (state.canManage) {
                        OutlinedButton(onClick = { onNavigate(Route.RecommendationEditor(item.id)) }) { Text("Editar indicação") }
                        TextButton(onClick = { pendingDelete = item }, enabled = !state.saving) { Text("Excluir indicação", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    pendingDelete?.takeIf { state.canManage }?.let { item ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Excluir indicação?") },
            text = { Text("${item.title} será removida. Esta ação não pode ser desfeita.") },
            confirmButton = { TextButton(enabled = !state.saving, onClick = { pendingDelete = null; viewModel.delete(item.id) }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Voltar") } })
    }
}

@Composable
fun RecommendationEditor(id: String, viewModel: RecommendationsViewModel, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.reload() }
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }
    EditorRecord(id, state.items, Recommendation::id, state.loaded, state.failure, viewModel::reload) { item ->
        var title by rememberSaveable { mutableStateOf(item?.title.orEmpty()) }
        var description by rememberSaveable { mutableStateOf(item?.description.orEmpty()) }
        var contact by rememberSaveable { mutableStateOf(item?.contact.orEmpty()) }
        var link by rememberSaveable { mutableStateOf(item?.link.orEmpty()) }
        val draft = RecommendationDraft(title, description, contact, link)
        val valid = runCatching { draft.validated() }.isSuccess
        EditorColumn(item == null) {
            AppTextField("Título da indicação", title, { title = it.take(160) }, placeholder = "Ex.: Doces da Maria")
            AppTextArea("Descrição (mínimo de 5 caracteres)", description, { description = it.take(4000) })
            AppTextField("Contato (opcional)", contact, { contact = it.take(200) }, placeholder = "Nome, telefone ou e-mail")
            AppTextField("Link (opcional)", link, { link = it.take(2048) }, placeholder = "https://...")
            FieldHint("Publique apenas contatos e materiais autorizados pela pessoa indicada. Links devem começar com https://.")
            Button(onClick = { viewModel.save(item?.id, draft) }, enabled = valid && !state.saving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(if (state.saving) "Salvando…" else "Salvar indicação", fontSize = 18.sp) }
        }
    }
}
