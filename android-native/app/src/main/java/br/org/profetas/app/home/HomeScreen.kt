package br.org.profetas.app.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.R
import br.org.profetas.app.church.ChurchViewModel
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.intent.openLink
import br.org.profetas.app.support.ui.LoadFailure
import br.org.profetas.app.support.ui.PrimaryButton
import br.org.profetas.app.support.ui.QuickAction
import br.org.profetas.app.support.ui.SectionCard
import br.org.profetas.app.support.ui.SectionTitle
import br.org.profetas.app.support.ui.SectionUnderline
import java.time.Year

private const val UPCOMING_LIMIT = 5

private val sectionCardShape = RoundedCornerShape(14.dp)

// Tela Início, a partir de app/(tabs)/index.tsx: as medidas (margens, fontes, raios) são as do StyleSheet de lá.
@Composable
fun HomeScreen(
    eventsViewModel: EventsViewModel,
    churchViewModel: ChurchViewModel,
    greetingName: String?,
    storageBaseUrl: String,
    onNavigate: (Route) -> Unit,
) {
    val eventsState by eventsViewModel.state.collectAsStateWithLifecycle()
    val churchState by churchViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp),
    ) {
        LogoHeader()

        Title(greetingName)

        QuickActions(onNavigate)

        SectionCard(modifier = sectionCardModifier) {
            SectionTitle("Quem somos")
            SectionUnderline()

            Paragraph(
                text = churchState.historyPreview,
                maxLines = 7,
            )

            OutlinedAction(label = "Saiba mais sobre nós", onClick = { onNavigate(Route.History) })
        }

        churchState.failure?.let { failure ->
            LoadFailure(
                message = failure,
                onRetry = churchViewModel::reload,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            )
        }

        UpcomingHeader(onSeeAll = { onNavigate(Route.Events) })

        eventsState.failure?.let { failure ->
            LoadFailure(
                message = failure,
                onRetry = eventsViewModel::reload,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        UpcomingEventsCarousel(
            events = eventsViewModel.upcoming(UPCOMING_LIMIT),
            isLoading = eventsState.loading,
            storageBaseUrl = storageBaseUrl,
            onOpen = { event -> onNavigate(Route.EventDetail(event.id)) },
        )

        WhereToFindUs(address = churchState.church.address)

        Text(
            text = "© ${Year.now().value} Profetas de Deus",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .padding(vertical = 12.dp),
        )
    }
}

// O Expo dá ao cartão de seção uma sombra forte (elevation 10); a sombra vem antes do recorte do SectionCard.
private val sectionCardModifier = Modifier
    .padding(start = 16.dp, end = 16.dp, top = 18.dp)
    .shadow(elevation = 10.dp, shape = sectionCardShape)

@Composable
private fun LogoHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MaterialTheme.colorScheme.outline),
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Igreja Profetas de Deus",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Selo sobre o logo: fica sempre sobre a imagem, então usa preto translúcido e branco fixos como no Expo.
        Text(
            text = "Igreja",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White,
            modifier = Modifier
                .padding(start = 18.dp, top = 18.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Title(greetingName: String?) {
    Text(
        text = "Igreja Profetas de Deus",
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.2.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 8.dp),
    )

    greetingName?.takeIf { name -> name.isNotBlank() }?.let { name ->
        Text(
            text = "Que bom ter você aqui, $name.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 4.dp),
        )
    }
}

@Composable
private fun QuickActions(onNavigate: (Route) -> Unit) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickAction(
            emoji = "🙏",
            label = "Oração",
            onClick = { onNavigate(Route.Prayer) },
            modifier = Modifier.weight(1f),
        )

        QuickAction(
            emoji = "🤝",
            label = "Projetos",
            onClick = { onNavigate(Route.Campaigns) },
            modifier = Modifier.weight(1f),
        )

        QuickAction(
            emoji = "🎂",
            label = "Parabéns",
            onClick = { onNavigate(Route.Birthdays) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Paragraph(text: String, maxLines: Int = Int.MAX_VALUE) {
    Text(
        text = text,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        // marginBottom 10 do cabeçalho + marginTop 8 do texto no Expo.
        modifier = Modifier.padding(top = 18.dp),
    )
}

@Composable
private fun OutlinedAction(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)

    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 14.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun UpcomingHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Próximos Eventos",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = "Ver todos",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onSeeAll),
        )
    }
}

@Composable
private fun WhereToFindUs(address: String) {
    val context = LocalContext.current

    SectionCard(modifier = sectionCardModifier) {
        SectionTitle("Onde nos encontrar")

        Paragraph(text = address.ifBlank { "O endereço da igreja ainda não foi publicado." })

        if (address.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))

            PrimaryButton(
                label = "Como chegar",
                onClick = { openLink(context, "geo:0,0?q=${Uri.encode(address)}") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
