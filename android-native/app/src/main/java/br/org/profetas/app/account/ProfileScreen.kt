package br.org.profetas.app.account

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.ui.AppTextField
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.ScreenColumn
import br.org.profetas.app.support.ui.SectionCard
import br.org.profetas.app.support.ui.SectionTitle

// Não há tela de perfil no Expo: usa os componentes compartilhados das outras telas convertidas.
@Composable
fun ProfileScreen(viewModel: AccountViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadProfile()
        }
    }

    if (!isLoggedIn) {
        ScreenColumn {
            EmptyState(text = "Entre na sua conta para editar o perfil.")
        }

        return
    }

    ScreenColumn(modifier = Modifier.padding(vertical = 16.dp)) {
        NameCard(viewModel)

        PasswordCard(viewModel)
    }
}

@Composable
private fun NameCard(viewModel: AccountViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var name by rememberSaveable(profile?.id, profile?.name) { mutableStateOf(profile?.name.orEmpty()) }

    SectionCard {
        SectionTitle(title = "Meu perfil")

        Text(
            text = viewModel.email,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        AppTextField(
            label = "Nome completo",
            value = name,
            onChange = { input -> name = input.take(MAX_PERSON_NAME_LENGTH) },
        )

        BlockButton(
            label = "Salvar perfil",
            enabled = !state.saving,
            onClick = { viewModel.saveName(name) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun PasswordCard(viewModel: AccountViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Senhas não entram no estado salvo da Activity: ele sobrevive à morte do processo, e a senha não deve.
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == AccountEvent.PASSWORD_UPDATED) {
                password = ""
                confirmation = ""
            }
        }
    }

    SectionCard {
        SectionTitle(title = "Alterar senha")

        AppTextField(
            label = "Nova senha",
            value = password,
            onChange = { input -> password = input },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(top = 16.dp),
        )

        AppTextField(
            label = "Repita a nova senha",
            value = confirmation,
            onChange = { input -> confirmation = input },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(top = 16.dp),
        )

        BlockButton(
            label = "Atualizar senha",
            enabled = !state.saving,
            onClick = { viewModel.updatePassword(password, confirmation) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
