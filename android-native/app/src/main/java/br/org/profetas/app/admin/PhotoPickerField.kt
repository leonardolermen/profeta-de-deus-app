package br.org.profetas.app.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage

private const val KEEP = "keep"
private const val REMOVE = "remove"

// O Uri escolhido sobrevive à rotação da tela junto com os outros campos do editor.
private val PHOTO_INTENT_SAVER = Saver<PhotoIntent, String>(
    save = { photo ->
        when (photo) {
            PhotoIntent.Keep -> KEEP
            PhotoIntent.Remove -> REMOVE
            is PhotoIntent.Replace -> photo.source.toString()
        }
    },
    restore = { saved ->
        when (saved) {
            KEEP -> PhotoIntent.Keep
            REMOVE -> PhotoIntent.Remove
            else -> PhotoIntent.Replace(saved.toUri())
        }
    },
)

@Composable
fun rememberPhotoIntent(): MutableState<PhotoIntent> =
    rememberSaveable(stateSaver = PHOTO_INTENT_SAVER) { mutableStateOf(PhotoIntent.Keep) }

// Photo Picker do sistema: não pede permissão de leitura da galeria, e o app só recebe a foto que o admin escolheu.
// `storedUrl` é a foto já gravada (null se não há); `onRemove` null quando o registro não pode ficar sem foto.
@Composable
fun PhotoPickerField(
    storedUrl: String?,
    photo: PhotoIntent,
    onChange: (PhotoIntent) -> Unit,
    isUploading: Boolean,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked: Uri? ->
        picked?.let { source -> onChange(PhotoIntent.Replace(source)) }
    }

    val preview: Any? = when (photo) {
        PhotoIntent.Keep -> storedUrl
        PhotoIntent.Remove -> null
        is PhotoIntent.Replace -> photo.source
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Foto",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp),
        )

        PhotoPreview(preview = preview, isUploading = isUploading)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                enabled = !isUploading,
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            ) {
                Text(if (preview == null) "Escolher foto" else "Trocar foto")
            }

            if (onRemove != null && preview != null) {
                TextButton(enabled = !isUploading, onClick = onRemove) {
                    Text("Remover foto")
                }
            }
        }
    }
}

@Composable
private fun PhotoPreview(preview: Any?, isUploading: Boolean) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (preview == null) {
            Text(text = "Nenhuma foto escolhida", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            AsyncImage(
                model = preview,
                contentDescription = "Prévia da foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
