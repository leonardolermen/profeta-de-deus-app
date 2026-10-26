package br.org.profetas.app.account

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.ui.AppTextField

// Sem equivalente no Expo (que não tinha recuperação): usa o mesmo molde de login e cadastro.
@Composable
fun RecoveryScreen(viewModel: AccountViewModel, onLogin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }

    AuthPage {
        AuthHeader(
            emoji = "🔑",
            title = "Recuperar acesso",
            subtitle = "Enviaremos um link para o e-mail informado. " +
                "Abra o link neste aparelho para definir uma nova senha.",
        )

        AppTextField(
            label = "E-mail",
            value = email,
            onChange = { input -> email = input },
            placeholder = "seu@email.com",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
            ),
            cornerRadius = 16.dp,
        )

        GradientSubmitButton(
            label = "Enviar instruções",
            isSending = state.saving,
            onClick = { viewModel.requestRecovery(email) },
            modifier = Modifier.padding(top = 32.dp),
        )

        AuthFooter(
            text = "Lembrou a senha? ",
            link = "Voltar ao login",
            onClick = onLogin,
            modifier = Modifier.padding(top = 32.dp),
        )
    }
}
