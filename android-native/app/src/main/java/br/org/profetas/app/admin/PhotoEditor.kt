package br.org.profetas.app.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.gallery.Photo
import br.org.profetas.app.support.ui.AppTextField

private const val MAX_TITLE_INPUT = 160
private const val MAX_ALBUM_INPUT = 120

// Sem campo de URL: foto nova sempre vem do Photo Picker para o bucket galeria. A foto antiga com URL externa
// continua com ela até o admin escolher outra imagem.
@Composable
fun PhotoEditor(photoId: String, viewModel: AdminViewModel, storageBaseUrl: String, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    val albums = state.photos
        .map { photo -> photo.album.trim() }
        .filter { album -> album.isNotEmpty() }
        .distinct()
        .sorted()

    EditorRecord(
        id = photoId,
        records = state.photos,
        recordId = Photo::id,
        hasLoaded = state.hasLoaded,
        failure = state.failure,
        onRetry = viewModel::reload,
    ) { photo ->
        PhotoFields(
            photo = photo,
            albums = albums,
            isSaving = state.saving,
            storageBaseUrl = storageBaseUrl,
            onSave = { title, album, intent -> viewModel.savePhoto(photo, title, album, intent) },
        )
    }
}

@Composable
private fun PhotoFields(
    photo: Photo?,
    albums: List<String>,
    isSaving: Boolean,
    storageBaseUrl: String,
    onSave: (title: String, album: String, photo: PhotoIntent) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(photo?.title.orEmpty()) }
    var album by rememberSaveable { mutableStateOf(photo?.album.orEmpty()) }
    var image by rememberPhotoIntent()

    EditorColumn(isNew = photo == null) {
        AppTextField(label = "Título", value = title, onChange = { input -> title = input.take(MAX_TITLE_INPUT) })

        AppTextField(label = "Álbum", value = album, onChange = { input -> album = input.take(MAX_ALBUM_INPUT) })

        if (albums.isNotEmpty()) {
            AlbumSuggestions(albums = albums, selected = album.trim(), onSelect = { choice -> album = choice })
        }

        PhotoPickerField(
            storedUrl = photo?.imageUrl(storageBaseUrl),
            photo = image,
            onChange = { change -> image = change },
            isUploading = isSaving && image is PhotoIntent.Replace,
            onRemove = null,
        )

        FieldHint("Publique somente imagens autorizadas.")

        SaveButton(isSaving = isSaving) {
            onSave(title, album, image)
        }
    }
}

@Composable
private fun AlbumSuggestions(albums: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        albums.forEach { album ->
            FilterChip(
                selected = album == selected,
                onClick = { onSelect(album) },
                label = { Text(album) },
                colors = churchChipColors(),
            )
        }
    }
}
