package br.org.profetas.app.church

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.intent.openLink
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure

// Contato, a partir de app/contato.tsx. O Expo mostrava telefone, e-mail e endereço fixos e só avisava que a ação
// "viria em breve"; aqui cada cartão usa o dado cadastrado da igreja, abre o aplicativo certo e some se o dado falta.
@Composable
fun ContactScreen(viewModel: ChurchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val church = state.church
    val context = LocalContext.current

    val hasContact = listOf(church.whatsapp, church.phone, church.email, church.address)
        .any { field -> field.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        Header()

        state.failure?.let { failure ->
            LoadFailure(message = failure, onRetry = viewModel::reload, modifier = Modifier.padding(bottom = 16.dp))
        }

        when {
            hasContact -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (church.whatsapp.isNotBlank()) {
                    ContactMethodCard(
                        icon = "💬",
                        title = "WhatsApp Secretaria",
                        description = church.whatsapp,
                        style = ContactCardStyle.Highlight,
                        onClick = { openLink(context, ContactLinks.whatsapp(church.whatsapp)) },
                    )
                }

                if (church.phone.isNotBlank()) {
                    ContactMethodCard(
                        icon = "📞",
                        title = "Ligar para Igreja",
                        description = church.phone,
                        onClick = { openLink(context, ContactLinks.phone(church.phone)) },
                    )
                }

                if (church.email.isNotBlank()) {
                    ContactMethodCard(
                        icon = "✉️",
                        title = "Enviar um E-mail",
                        description = church.email,
                        onClick = { openLink(context, ContactLinks.email(church.email)) },
                    )
                }

                if (church.address.isNotBlank()) {
                    AddressBox(
                        address = church.address,
                        onOpenMap = { openLink(context, ContactLinks.map(church.address)) },
                    )
                }
            }

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            // Com falha, o cartão acima já explica; dizer "não publicado" aqui esconderia que o problema é a rede.
            state.failure == null -> EmptyState("Os contatos ainda não foram publicados pela secretaria.")
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "📲", fontSize = 54.sp, modifier = Modifier.padding(bottom = 8.dp))

        Text(
            text = "Entre em Contato",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Nossa secretaria e corpo pastoral estão sempre à disposição para te ajudar no que for preciso.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun AddressBox(address: String, onOpenMap: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "📍 Nosso Endereço",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        Text(
            text = address,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        Text(
            text = "Abrir no Mapa",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(role = Role.Button, onClick = onOpenMap)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}
