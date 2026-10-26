package br.org.profetas.app.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.EmptyState
import coil.compose.AsyncImage

// Foto aberta pela galeria. Não existe no Expo: segue o mesmo respiro de 20 e os cantos de 16 da grade.
@Composable
fun PhotoScreen(photoId: String, viewModel: GalleryViewModel, storageBaseUrl: String) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photo = state.photos.find { candidate -> candidate.id == photoId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        if (photo == null) {
            EmptyState("Foto indisponível.")

            return@Column
        }

        val imageUrl = photo.imageUrl(storageBaseUrl)

        AsyncImage(
            model = imageUrl,
            contentDescription = photo.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.outline),
        )

        Text(
            text = photo.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )

        if (photo.album.isNotBlank()) {
            Text(
                text = photo.album,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (imageUrl != null) {
            BlockButton(
                label = "Compartilhar foto",
                onClick = { share(context, "${photo.title}\n$imageUrl") },
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}
