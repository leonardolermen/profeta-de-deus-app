package br.org.profetas.app.event.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.BorderedCard
import br.org.profetas.app.support.ui.EmptyState

// Sem equivalente no Expo: mesma página de app/campanhas.tsx (título, subtítulo e cartões com borda).
@Composable
fun MyRegistrationsScreen(viewModel: EventsViewModel, accountId: String?, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggedIn = accountId != null

    var cancelling by rememberSaveable { mutableStateOf<String?>(null) }

    // Chave pela conta, não pelo booleano: a troca A→B pelo link de recuperação não passa por "deslogado".
    LaunchedEffect(accountId) {
        viewModel.reloadRegistrations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Suas inscrições em eventos da igreja.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            !isLoggedIn -> {
                EmptyState("Entre na sua conta para ver suas inscrições.")

                BlockButton(label = "Entrar", onClick = { onNavigate(Route.Login) })
            }

            state.registrations.isEmpty() -> EmptyState("Você ainda não tem inscrições.")

            else -> state.registrations.forEach { registration ->
                RegistrationCard(
                    registration = registration,
                    event = state.events.find { event -> event.id == registration.eventId },
                    isSaving = state.isSaving,
                    onOpen = { onNavigate(Route.EventDetail(registration.eventId)) },
                    onCancel = { cancelling = registration.id },
                )
            }
        }
    }

    cancelling?.let { id ->
        CancelDialog(
            onConfirm = {
                cancelling = null
                viewModel.cancelRegistration(id)
            },
            onDismiss = { cancelling = null },
        )
    }
}

@Composable
private fun RegistrationCard(
    registration: Registration,
    event: Event?,
    isSaving: Boolean,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
) {
    BorderedCard {
        event?.let { found ->
            Text(
                text = found.type.label.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = displayDate(found.date),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
        }

        Text(
            text = event?.title ?: "Evento",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "${registration.name} · ${registration.phone}",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            TextButton(onClick = onOpen) {
                Text("Ver evento", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cancelar inscrição", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CancelDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancelar inscrição?") },
        text = { Text("Você poderá se inscrever novamente se o evento ainda estiver disponível.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cancelar inscrição")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Manter")
            }
        },
    )
}
