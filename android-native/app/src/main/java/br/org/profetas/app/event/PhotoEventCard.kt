package br.org.profetas.app.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.support.storage.StorageBucket
import br.org.profetas.app.theme.Palette
import coil.compose.AsyncImage
import java.time.ZoneId

// Cartão de um evento do banco: a data já vem válida (timestamptz), por isso o selo não tem caminho de erro.
@Composable
fun PhotoEventCard(event: Event, storageBaseUrl: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PhotoEventCard(
        title = event.title,
        subtitle = subtitleOf(event),
        ticket = ticketDate(requireNotNull(eventInstant(event.date)), ZoneId.systemDefault()),
        image = imageOf(event, storageBaseUrl),
        onClick = onClick,
        modifier = modifier,
    )
}

// Cartão de evento em destaque com foto de fundo (specialEventCard/eventImageBg/imageOverlay de
// app/(tabs)/index.tsx). `image` aceita tanto um @DrawableRes (Int, imagem padrão do EventType)
// quanto uma URL (String, foto enviada pela igreja) — é o que o Coil espera como modelo.
@Composable
fun PhotoEventCard(
    title: String,
    subtitle: String,
    ticket: TicketDate,
    image: Any,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = image,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
        )

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DateTicket(ticket)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = ticket.time,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.OnPhoto,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Palette.OnPhoto,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Palette.OnPhoto.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// O Expo mostrava uma chamada curta sob o título; aqui ela é a primeira linha da descrição, ou o tipo do evento.
private fun subtitleOf(event: Event): String =
    event.description.lineSequence().firstOrNull { line -> line.isNotBlank() } ?: event.type.label

private fun imageOf(event: Event, storageBaseUrl: String): Any =
    event.imagePath?.let { path -> StorageBucket.EVENTS.publicUrl(storageBaseUrl, path) } ?: event.type.defaultImage
