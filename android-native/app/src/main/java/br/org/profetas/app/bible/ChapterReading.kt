package br.org.profetas.app.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.ui.LoadFailure

// expandedContent/capitulosGrid/botaoNumero de app/(tabs)/explore.tsx: a grade de capítulos do livro aberto.
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChapterPicker(book: Book, activeNumber: Int?, onChapter: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        SectionHeading("ESCOLHA O CAPÍTULO")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            (1..book.chapters).forEach { number ->
                ChapterButton(number = number, isActive = number == activeNumber, onClick = { onChapter(number) })
            }
        }
    }
}

@Composable
private fun ChapterButton(number: Int, isActive: Boolean, onClick: () -> Unit) {
    val background = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val border = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val content = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "$number", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = content)
    }
}

// Onde o Expo mostrava a grade de versículos e um texto simulado, aqui vem o capítulo inteiro da API.
@Composable
internal fun ChapterReading(state: BibleState, onRetry: () -> Unit, onToggleFavorite: (String) -> Unit) {
    val opened = state.opened ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        RowDivider(MaterialTheme.colorScheme.surface)

        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeading("CAPÍTULO ${opened.number} - VERSÍCULOS")

            state.failure?.let { failure ->
                LoadFailure(message = failure, onRetry = onRetry)
            }

            if (state.isLoadingChapter) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }

            state.chapter?.let { chapter ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    chapter.verses.forEach { verse ->
                        val verseKey = FavoriteVerses.keyOf(chapter.book, chapter.number, verse)

                        VerseItem(
                            verse = verse,
                            isFavorite = verseKey in state.favorites,
                            verseKey = verseKey,
                            onToggleFavorite = { onToggleFavorite(verseKey) },
                        )
                    }
                }
            }
        }
    }
}

// escrituraBox do Expo, um por versículo: o número no badge azul, o texto, e favoritar/compartilhar embaixo.
@Composable
private fun VerseItem(verse: Verse, isFavorite: Boolean, verseKey: String, onToggleFavorite: () -> Unit) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${verse.number}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = verse.text,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                VerseAction(label = if (isFavorite) "★ Favorito" else "☆ Favoritar", onClick = onToggleFavorite)

                VerseAction(label = "Compartilhar", onClick = { share(context, verseKey) })
            }
        }
    }
}

@Composable
internal fun VerseAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
    )
}

@Composable
internal fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
    )
}
