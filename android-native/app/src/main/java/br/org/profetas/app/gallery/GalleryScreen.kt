package br.org.profetas.app.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure
import coil.compose.AsyncImage

private const val ALL_ALBUMS = "Todos"

// O Expo usava uma lista fixa de alturas, uma por foto; as fotos reais não trazem proporção, então a mesma lista
// se repete pela ordem para manter o efeito de mural de duas colunas.
private val MASONRY_HEIGHTS: List<Dp> = listOf(200.dp, 260.dp, 240.dp, 180.dp, 220.dp, 280.dp)

// Galeria, a partir de app/galeria.tsx: título, subtítulo e duas colunas de fotos com cantos de 16. O filtro por
// álbum não existe no Expo e foi mantido da tela anterior.
@Composable
fun GalleryScreen(viewModel: GalleryViewModel, storageBaseUrl: String, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var album by rememberSaveable { mutableStateOf(ALL_ALBUMS) }

    val albumNames = state.photos
        .map { photo -> photo.album }
        .filter { name -> name.isNotBlank() }
        .distinct()
    val photos = state.photos.filter { photo -> album == ALL_ALBUMS || photo.album == album }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        Header()

        AlbumFilter(albums = listOf(ALL_ALBUMS) + albumNames, selected = album, onSelect = { name -> album = name })

        state.failure?.let { failure ->
            LoadFailure(message = failure, onRetry = viewModel::reload, modifier = Modifier.padding(bottom = 16.dp))
        }

        when {
            photos.isNotEmpty() -> Masonry(photos, storageBaseUrl) { photo -> onNavigate(Route.Photo(photo.id)) }

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            else -> EmptyState("Nenhuma foto publicada neste álbum.")
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = "Momentos Inesquecíveis",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Recordações dos nossos cultos, retiros, encontros e sacramentos.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlbumFilter(albums: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        albums.forEach { name ->
            FilterChip(selected = selected == name, onClick = { onSelect(name) }, label = { Text(name) })
        }
    }
}

@Composable
private fun Masonry(photos: List<Photo>, storageBaseUrl: String, onOpen: (Photo) -> Unit) {
    val indexed = photos.withIndex()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(0, 1).forEach { columnIndex ->
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                indexed.filter { item -> item.index % 2 == columnIndex }.forEach { item ->
                    PhotoTile(
                        photo = item.value,
                        height = MASONRY_HEIGHTS[item.index % MASONRY_HEIGHTS.size],
                        storageBaseUrl = storageBaseUrl,
                        onOpen = onOpen,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(photo: Photo, height: Dp, storageBaseUrl: String, onOpen: (Photo) -> Unit) {
    AsyncImage(
        model = photo.imageUrl(storageBaseUrl),
        contentDescription = photo.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.outline)
            .clickable { onOpen(photo) },
    )
}
