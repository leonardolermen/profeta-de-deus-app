package br.org.profetas.app.campaign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.event.dayAndMonth
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.BorderedCard
import br.org.profetas.app.support.ui.LoadFailure

// Projetos, a partir de app/campanhas.tsx: as medidas são as do StyleSheet de lá. O Expo listava também
// campanhas passadas e o "Quero Participar" não fazia nada; aqui só entram as futuras e o botão abre o detalhe
// para a inscrição.
@Composable
fun CampaignsScreen(viewModel: EventsViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val campaigns = viewModel.campaigns()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        Text(
            text = "Nossos Projetos",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Engaje-se nas campanhas do nosso ministério e faça a diferença na vida das pessoas!",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        state.failure?.let { failure ->
            LoadFailure(message = failure, onRetry = viewModel::reload, modifier = Modifier.padding(bottom = 16.dp))
        }

        when {
            campaigns.isNotEmpty() -> campaigns.forEach { campaign ->
                CampaignCard(campaign, onParticipate = { onNavigate(Route.EventDetail(campaign.id)) })
            }

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            else -> NoCampaigns()
        }
    }
}

@Composable
private fun CampaignCard(campaign: Event, onParticipate: () -> Unit) {
    BorderedCard(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(48.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🎯", fontSize = 24.sp)
            }

            Column {
                Text(
                    text = "CAMPANHA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = dayAndMonth(campaign.date),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        Text(
            text = campaign.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = campaign.description,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        BlockButton(label = "Quero Participar", onClick = onParticipate)
    }
}

@Composable
private fun NoCampaigns() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "📋", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))

        Text(
            text = "Nenhuma campanha ativa no momento.",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
