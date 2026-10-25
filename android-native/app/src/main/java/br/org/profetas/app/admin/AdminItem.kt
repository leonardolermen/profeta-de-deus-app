package br.org.profetas.app.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.ui.SectionCard

const val DEFAULT_DELETE_WARNING = "Esta ação não pode ser desfeita."

// Excluir sempre passa pela confirmação: `warning` é o texto do diálogo, `perform` o que roda ao confirmar.
class DeleteAction(val label: String, val warning: String, val perform: () -> Unit)

class EditAction(val label: String, val open: () -> Unit)

// Uma linha de qualquer seção da Administração, já com o que o admin pode fazer com ela.
class AdminItem(
    val title: String,
    val subtitle: String,
    val body: String? = null,
    val edit: EditAction? = null,
    val delete: DeleteAction? = null,
)

fun matchesQuery(query: String, vararg texts: String?): Boolean {
    val wanted = query.trim()

    return wanted.isEmpty() || texts.any { text -> text?.contains(wanted, ignoreCase = true) == true }
}

@Composable
fun AdminItemCard(item: AdminItem, onDelete: (DeleteAction) -> Unit) {
    SectionCard {
        Text(
            text = item.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (item.subtitle.isNotEmpty()) {
            Text(
                text = item.subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        item.body?.let { body ->
            Text(
                text = body,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
            item.edit?.let { edit ->
                TextButton(onClick = edit.open) {
                    Text(edit.label)
                }
            }

            item.delete?.let { delete ->
                TextButton(onClick = { onDelete(delete) }) {
                    Text(delete.label, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
