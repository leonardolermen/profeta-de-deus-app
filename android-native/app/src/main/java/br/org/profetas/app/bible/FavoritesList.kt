package br.org.profetas.app.bible

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.ui.BorderedCard
import br.org.profetas.app.support.ui.EmptyState

// Versículos marcados durante a leitura (não existia no Expo; vem da tela Kotlin anterior).
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun FavoritesList(favorites: Set<String>, onRemove: (String) -> Unit) {
    if (favorites.isEmpty()) {
        EmptyState("Marque versículos durante a leitura para encontrá-los aqui.")

        return
    }

    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        favorites.sorted().forEach { verseKey ->
            BorderedCard {
                Text(
                    text = FavoriteVerses.referenceOf(verseKey),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = FavoriteVerses.textOf(verseKey),
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    VerseAction(label = "Compartilhar", onClick = { share(context, verseKey) })

                    VerseAction(label = "Remover favorito", onClick = { onRemove(verseKey) })
                }
            }
        }
    }
}
