package br.org.profetas.app.event.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventType
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.AppTextField
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.BorderedCard

private const val MAX_NAME_LENGTH = 120

private const val MAX_PHONE_LENGTH = 25

// Inscrição no detalhe do evento: visitante é mandado entrar, quem já se inscreveu vê a confirmação, os demais
// preenchem o formulário. Textos e regras são os da tela Kotlin anterior.
@Composable
fun RegistrationSection(
    event: Event,
    registration: Registration?,
    isLoggedIn: Boolean,
    profileName: String?,
    isSaving: Boolean,
    onEnroll: (name: String, phone: String) -> Unit,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    BorderedCard(modifier) {
        when {
            !isLoggedIn -> {
                CardTitle(headingOf(event))

                BlockButton(
                    label = "Entrar para se inscrever",
                    onClick = { onNavigate(Route.Login) },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            registration != null -> Confirmation(registration, onManage = { onNavigate(Route.MyRegistrations) })

            else -> Form(event, profileName, isSaving, onEnroll, onOpenPrivacy = { onNavigate(Route.Privacy) })
        }
    }
}

@Composable
private fun Confirmation(registration: Registration, onManage: () -> Unit) {
    CardTitle("Inscrição confirmada")

    Text(
        text = registration.name,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp),
    )

    TextButton(onClick = onManage) {
        Text("Gerenciar inscrição", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Form(
    event: Event,
    profileName: String?,
    isSaving: Boolean,
    onEnroll: (name: String, phone: String) -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    // O perfil chega depois da tela: quando ele chega, o nome vem preenchido.
    var name by rememberSaveable(profileName) { mutableStateOf(profileName.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf("") }
    var accepted by rememberSaveable { mutableStateOf(false) }

    CardTitle(headingOf(event))

    AppTextField(
        label = "Nome completo",
        value = name,
        onChange = { text -> name = text.take(MAX_NAME_LENGTH) },
        modifier = Modifier.padding(top = 16.dp),
    )

    AppTextField(
        label = "Telefone com DDD",
        value = phone,
        onChange = { text -> phone = text.take(MAX_PHONE_LENGTH) },
        modifier = Modifier.padding(top = 12.dp),
    )

    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clickable { accepted = !accepted },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = accepted, onCheckedChange = { checked -> accepted = checked })

        Text(
            text = "Autorizo o contato da igreja sobre esta inscrição.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }

    TextButton(onClick = onOpenPrivacy) {
        Text("Consultar privacidade")
    }

    val isComplete = name.trim().length >= 2 && phone.count { character -> character.isDigit() } >= 8 && accepted

    BlockButton(
        label = "Confirmar inscrição",
        onClick = { onEnroll(name, phone) },
        enabled = !isSaving && isComplete,
        modifier = Modifier.padding(top = 4.dp),
    )
}

// O batismo tem chamada própria, como o botão "Quero me batizar" da tela de Batismo.
private fun headingOf(event: Event): String =
    if (event.type == EventType.BATISMO) "Quero me batizar" else "Participar deste evento"

@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
