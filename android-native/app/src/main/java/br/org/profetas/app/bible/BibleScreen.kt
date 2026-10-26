package br.org.profetas.app.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.LoadFailure

// Aba Bíblia, a partir de app/(tabs)/explore.tsx: as medidas são as do StyleSheet de lá. O Expo mostrava um texto
// simulado; aqui o capítulo vem da API da Bíblia (NVI), e os favoritos com compartilhar vêm da tela Kotlin anterior.
@Composable
fun BibleScreen(viewModel: BibleViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var expandedBook by rememberSaveable { mutableStateOf(state.opened?.book?.abbreviation) }
    var isShowingFavorites by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

    // O cabeçalho fica parado e só a lista rola, como no Expo. A barra de status já vem no padding do Scaffold.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 24.dp),
    ) {
        Header(
            isShowingFavorites = isShowingFavorites,
            onStudies = { onNavigate(Route.Studies) },
            onFavorites = { isShowingFavorites = !isShowingFavorites },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 60.dp),
        ) {
            val booksFailure = state.failure.takeIf { state.opened == null }
            booksFailure?.let { failure ->
                LoadFailure(message = failure, onRetry = viewModel::reload, modifier = Modifier.padding(bottom = 16.dp))
            }

            when {
                isShowingFavorites -> FavoritesList(
                    favorites = state.favorites,
                    onRemove = viewModel::toggleFavorite,
                )

                state.books.isEmpty() && state.isLoadingBooks -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .align(Alignment.CenterHorizontally),
                )

                else -> BookList(
                    state = state,
                    expandedBook = expandedBook,
                    onBook = { book ->
                        expandedBook = if (expandedBook == book.abbreviation) null else book.abbreviation
                        viewModel.closeChapter()
                    },
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun Header(isShowingFavorites: Boolean, onStudies: () -> Unit, onFavorites: () -> Unit) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
        Text(
            text = "Bíblia Sagrada",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Text(
            text = "Selecione um livro para começar a leitura",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton(icon = "📚", label = "Estudos e Trilhas", isSelected = false, onClick = onStudies)

            HeaderButton(icon = "⭐", label = "Favoritos", isSelected = isShowingFavorites, onClick = onFavorites)
        }
    }
}

// studiesBtn do Expo. O chip de Favoritos usa o mesmo desenho e, ligado, inverte as cores como o capítulo ativo.
@Composable
private fun HeaderButton(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val content = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))

        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = content)
    }
}

@Composable
private fun BookList(
    state: BibleState,
    expandedBook: String?,
    onBook: (Book) -> Unit,
    viewModel: BibleViewModel,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
    ) {
        state.books.forEachIndexed { index, book ->
            val isExpanded = expandedBook == book.abbreviation

            BookRow(book = book, isExpanded = isExpanded, onClick = { onBook(book) })

            if (index != state.books.lastIndex) {
                RowDivider(MaterialTheme.colorScheme.outline)
            }

            if (isExpanded) {
                ChapterPicker(
                    book = book,
                    activeNumber = state.opened?.takeIf { opened -> opened.book == book }?.number,
                    onChapter = { number -> viewModel.openChapter(book, number) },
                )
            }

            if (isExpanded && state.opened?.book == book) {
                ChapterReading(
                    state = state,
                    onRetry = viewModel::reload,
                    onToggleFavorite = viewModel::toggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun BookRow(book: Book, isExpanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = book.name,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = if (isExpanded) "˄" else "˅",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
internal fun RowDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}
