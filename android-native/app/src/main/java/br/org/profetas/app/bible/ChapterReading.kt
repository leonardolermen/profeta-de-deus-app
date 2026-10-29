package br.org.profetas.app.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.ui.LoadFailure

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChapterReading(state: BibleState, viewModel: BibleViewModel, onFavorites: () -> Unit) {
    val opened = state.opened ?: return
    val chapter = state.chapter
    val verses = chapter?.verses.orEmpty()
    val selectedIndex = verses.indexOfFirst { it.number == state.selectedVerse }
    val selected = verses.getOrNull(selectedIndex)
    val context = LocalContext.current
    var showingLookup by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val verseScroll = rememberScrollState()

    LaunchedEffect(chapter, state.selectedVerse) { verseScroll.scrollTo(0) }

    LaunchedEffect(chapter, state.selectedVerse, state.continuousReading) {
        if (state.continuousReading && selectedIndex >= 0) listState.scrollToItem(selectedIndex)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = viewModel::closeChapter, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Trocar livro", fontSize = 17.sp)
            }
            TextButton(onClick = onFavorites, modifier = Modifier.heightIn(min = 48.dp)) { Text("Favoritos", fontSize = 17.sp) }
        }
        Text("${opened.book.name} ${opened.number}", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)
        OutlinedButton(onClick = { showingLookup = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Buscar livro, capítulo e versículo", fontSize = 17.sp)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tamanho da letra", fontSize = 16.sp, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { viewModel.changeFontSize(-2) }, enabled = state.fontSize > 18,
                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Diminuir letra" }) { Text("A−", fontSize = 18.sp) }
            OutlinedButton(onClick = { viewModel.changeFontSize(2) }, enabled = state.fontSize < 32,
                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Aumentar letra" }) { Text("A+", fontSize = 21.sp) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !state.continuousReading, onClick = { viewModel.setContinuousReading(false) },
                label = { Text("Por versículo", fontSize = 16.sp) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = state.continuousReading, onClick = { viewModel.setContinuousReading(true) },
                label = { Text("Capítulo inteiro", fontSize = 16.sp) }, modifier = Modifier.heightIn(min = 48.dp))
        }
        state.referenceFailure?.let { message ->
            Text(message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoadingChapter -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.failure != null -> LoadFailure(state.failure, viewModel::reload)
                verses.isEmpty() -> Text("Este capítulo não possui versículos disponíveis.", fontSize = 18.sp)
                state.continuousReading -> LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(verses, key = { it.number }) { verse ->
                        Text("${verse.number}  ${verse.text}", fontSize = state.fontSize.sp,
                            lineHeight = (state.fontSize * 1.5).sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth()
                                .background(if (verse.number == state.selectedVerse) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background)
                                .clickable { viewModel.selectVerse(verse.number) }
                                .padding(12.dp))
                    }
                }
                selected != null -> Column(Modifier.fillMaxSize().verticalScroll(verseScroll), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Versículo ${selected.number} de ${verses.last().number}", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(selected.text, fontSize = state.fontSize.sp, lineHeight = (state.fontSize * 1.5).sp,
                        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }

        if (selected != null && !state.isLoadingChapter) {
            val key = FavoriteVerses.keyOf(opened.book.name, opened.number, selected)
            if (state.continuousReading) Text("Selecionado: ${opened.book.name} ${opened.number}:${selected.number}", fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.toggleFavorite(key) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text(if (key in state.favorites) "★ Favorito" else "☆ Favoritar", fontSize = 16.sp)
                }
                OutlinedButton(onClick = { share(context, key) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text("Compartilhar", fontSize = 16.sp)
                }
            }
        }
        if (!state.continuousReading) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.moveVerse(-1) }, enabled = selectedIndex > 0 && !state.isLoadingChapter,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Anterior", fontSize = 18.sp) }
            Button(onClick = { viewModel.moveVerse(1) }, enabled = selectedIndex in 0 until verses.lastIndex && !state.isLoadingChapter,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text("Próximo", fontSize = 18.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.moveChapter(-1) }, enabled = opened.number > 1 && !state.isLoadingChapter,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Capítulo anterior", fontSize = 15.sp) }
            TextButton(onClick = { viewModel.moveChapter(1) }, enabled = opened.number < opened.book.chapters && !state.isLoadingChapter,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Próximo capítulo", fontSize = 15.sp) }
        }
    }

    if (showingLookup) AlertDialog(
        onDismissRequest = { showingLookup = false },
        title = { Text("Ir para uma passagem") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                PassageLookup(state.books, BiblePassage(opened.book, opened.number, state.selectedVerse)) {
                    viewModel.openPassage(it)
                    showingLookup = false
                }
            }
        },
        confirmButton = { TextButton(onClick = { showingLookup = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Fechar") } },
    )
}

@Composable
internal fun VerseAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
