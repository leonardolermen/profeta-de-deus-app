package br.org.profetas.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.org.profetas.app.AppState

@Composable fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp), content=content)
}
@Composable fun Heading(title: String, subtitle: String = "") {
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(title, style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold)
        if(subtitle.isNotBlank()) Text(subtitle, color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun Panel(title: String, subtitle: String = "", onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit = {}) {
    val inside: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(title, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold)
            if(subtitle.isNotBlank()) Text(subtitle, style=MaterialTheme.typography.bodyMedium)
            content()
        }
    }
    if(onClick == null) OutlinedCard(Modifier.fillMaxWidth(), content={ inside() })
    else OutlinedCard(onClick=onClick, modifier=Modifier.fillMaxWidth(), content={ inside() })
}
@Composable fun Field(label: String, value: String, change: (String) -> Unit, multiline: Boolean = false) {
    OutlinedTextField(value=value, onValueChange=change, label={ Text(label) }, modifier=Modifier.fillMaxWidth(), singleLine=!multiline, minLines=if(multiline) 4 else 1)
}
@Composable fun Empty(text: String) { Text(text, modifier=Modifier.padding(vertical=12.dp), color=MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable fun LoadStatus(state: AppState, section: String, retry: () -> Unit) {
    state.errors[section]?.let { error ->
        Panel("Não foi possível carregar", error) { TextButton(onClick=retry, enabled=!state.loading) { Text("Tentar novamente") } }
    }
}
fun openLink(context: Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    catch (_: ActivityNotFoundException) { Toast.makeText(context,"Nenhum aplicativo disponível para abrir este contato.",Toast.LENGTH_LONG).show() }
}
fun share(context: Context, text: String) {
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type="text/plain"; putExtra(Intent.EXTRA_TEXT,text) }, "Compartilhar"))
}
