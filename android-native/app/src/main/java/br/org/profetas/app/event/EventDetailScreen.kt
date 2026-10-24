package br.org.profetas.app.event

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.registration.RegistrationSection
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.message.UserMessages
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure
import br.org.profetas.app.theme.Palette
import coil.compose.AsyncImage
import java.time.ZoneId

// O Expo não tinha detalhe de evento: a foto de 220dp vem do topo de app/batismo.tsx e o selo, do cartão da Início;
// textos e regras de lembrete e inscrição são os da tela Kotlin anterior.
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventsViewModel,
    isLoggedIn: Boolean,
    profileName: String?,
    storageBaseUrl: String,
    userMessages: UserMessages,
    onNavigate: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Entrar ou sair da conta muda quais inscrições são do membro; os lembretes podem ter mudado nos Ajustes.
    LaunchedEffect(isLoggedIn) {
        viewModel.reloadRegistrations()
        viewModel.refreshReminders()
    }

    val event = state.events.find { candidate -> candidate.id == eventId }
    if (event == null) {
        MissingEvent(isLoading = state.loading, failure = state.failure, onRetry = viewModel::reload)

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp),
    ) {
        PhotoHeader(event, storageBaseUrl)

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Summary(event)

            if (isFuture(event.date)) {
                Actions(
                    event = event,
                    hasReminder = event.id in state.reminderIds,
                    viewModel = viewModel,
                    userMessages = userMessages,
                )

                RegistrationSection(
                    event = event,
                    registration = state.registrations.find { registration -> registration.eventId == event.id },
                    isLoggedIn = isLoggedIn,
                    profileName = profileName,
                    isSaving = state.isSaving,
                    onEnroll = { name, phone -> viewModel.enroll(event.id, name, phone) },
                    onNavigate = onNavigate,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                ShareAction(event, modifier = Modifier.padding(top = 24.dp))

                EmptyState("Este evento já aconteceu.")
            }
        }
    }
}

@Composable
private fun MissingEvent(isLoading: Boolean, failure: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        EmptyState(if (isLoading) "Carregando evento…" else "Evento indisponível.")

        failure?.let { message ->
            LoadFailure(message = message, onRetry = onRetry)
        }
    }
}

@Composable
private fun PhotoHeader(event: Event, storageBaseUrl: String) {
    val ticket = ticketDate(requireNotNull(eventInstant(event.date)), ZoneId.systemDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        AsyncImage(
            model = imageOf(event, storageBaseUrl),
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            DateTicket(ticket)

            Text(
                text = ticket.time,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.OnPhoto,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

// Tipo, título e data seguem cardType/mainTitle/cardDate de app/campanhas.tsx.
@Composable
private fun Summary(event: Event) {
    Text(
        text = event.type.label.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
    )

    Text(
        text = event.title,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 32.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp),
    )

    Text(
        text = displayDate(event.date),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )

    Text(
        text = event.description.ifBlank { "Mais informações serão publicadas pela igreja." },
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
private fun Actions(event: Event, hasReminder: Boolean, viewModel: EventsViewModel, userMessages: UserMessages) {
    val context = LocalContext.current

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.setReminder(event)
        } else {
            userMessages.show("Permita as notificações nas configurações do Android para receber o lembrete.")
        }
    }

    Row(
        modifier = Modifier.padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShareAction(event, modifier = Modifier.weight(1f))

        if (hasReminder) {
            ActionButton(
                emoji = "🔕",
                label = "Remover lembrete",
                onClick = { viewModel.removeReminder(event.id) },
                modifier = Modifier.weight(1f),
            )
        } else {
            ActionButton(
                emoji = "🔔",
                label = "Lembrar 1 hora antes",
                onClick = {
                    if (needsNotificationPermission(context)) {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setReminder(event)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    Text(
        text = "O horário do lembrete depende das condições de bateria e conexão do aparelho.",
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ShareAction(event: Event, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ActionButton(
        emoji = "📤",
        label = "Compartilhar",
        onClick = { share(context, "${event.title}\n${displayDate(event.date)}\n${event.description}") },
        modifier = modifier,
    )
}

// Só o Android 13+ pede a permissão de notificação em tempo de execução.
private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
