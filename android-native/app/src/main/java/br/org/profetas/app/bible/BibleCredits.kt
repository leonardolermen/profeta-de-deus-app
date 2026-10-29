package br.org.profetas.app.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun BibleCredits() {
    var showing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uri = LocalUriHandler.current
    TextButton(onClick = { showing = true }, modifier = Modifier.heightIn(min = 48.dp)) {
        Text("BLIVRE · Sobre a tradução e licença", fontSize = 16.sp)
    }
    if (showing) {
        val license by produceState("") {
            value = withContext(Dispatchers.IO) {
                context.assets.open("bible/LICENCA_BLIVRE.txt").bufferedReader().use { it.readText() }
            }
        }
        AlertDialog(
            onDismissRequest = { showing = false },
            title = { Text("Bíblia Livre") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(BibleEdition.CREDIT, fontSize = 17.sp)
                    Text("Contém Antigo e Novo Testamento. TR e N4 são variantes de base textual do Novo Testamento, não versões de linguagem antiga e moderna.")
                    TextButton(onClick = { uri.openUri(BibleEdition.SOURCE) }) { Text("Consultar a edição original") }
                    TextButton(onClick = { uri.openUri(BibleEdition.LICENSE_URL) }) { Text("Ver licença na Creative Commons") }
                    Text(license)
                }
            },
            confirmButton = { TextButton(onClick = { showing = false }) { Text("Fechar") } },
        )
    }
}
