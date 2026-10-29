package br.org.profetas.app.bible

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.LoadFailure

@Composable
fun BibleScreen(viewModel: BibleViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showingFavorites by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadBooks() }
    BackHandler(enabled = showingFavorites || state.opened != null) {
        if (showingFavorites) showingFavorites = false else viewModel.closeChapter()
    }

    when {
        showingFavorites -> Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            TextButton(onClick = { showingFavorites = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Voltar à Bíblia", fontSize = 17.sp)
            }
            Text("Meus favoritos", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            FavoritesList(state.favorites, viewModel::toggleFavorite) { key ->
                FavoriteVerses.passageOf(key, state.books)?.let {
                    showingFavorites = false
                    viewModel.openPassage(it)
                }
            }
        }
        state.opened != null -> ChapterReading(state, viewModel) { showingFavorites = true }
        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Bíblia Sagrada", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(BibleEdition.NAME, fontSize = 16.sp)
            BibleCredits()
            Text("Escolha um livro abaixo ou pesquise uma passagem.", fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onNavigate(Route.Studies) }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Text("Estudos", fontSize = 16.sp)
                }
                OutlinedButton(onClick = { showingFavorites = true }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Text("Favoritos", fontSize = 16.sp)
                }
            }
            if (state.isLoadingBooks) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.failure?.let { LoadFailure(it, viewModel::reload) }
            Text("Exemplo: escolha João, capítulo 3 e versículo 16. Deixe o versículo vazio para começar no início do capítulo.",
                fontSize = 16.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PassageLookup(books = state.books, isLoading = state.isLoadingBooks, showBooksInline = true, onOpen = viewModel::openPassage)
        }
    }
}
