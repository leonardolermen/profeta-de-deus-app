package br.org.profetas.app.account

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.ui.AppTextField

// app/auth/register.tsx. As regras de nome, e-mail e senha ficam nos value objects, aplicados pelo ViewModel: o Expo
// também deixava o botão sempre ativo e avisava ao tocar.
@Composable
fun RegisterScreen(viewModel: AccountViewModel, onSignedIn: () -> Unit, onLogin: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Senhas não entram no estado salvo da Activity: ele sobrevive à morte do processo, e a senha não deve.
    var password by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event == AccountEvent.SIGNED_IN) {
                onSignedIn()
            }
        }
    }

    AuthPage {
        AuthHeader(
            emoji = "✨",
            title = "Junte-se a nós",
            subtitle = "Crie sua conta para acompanhar seus eventos e dízimos.",
        )

        AppTextField(
            label = "Nome Completo",
            value = name,
            onChange = { input -> name = input.take(MAX_PERSON_NAME_LENGTH) },
            placeholder = "Ex: João da Silva",
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            cornerRadius = 16.dp,
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
            modifier = Modifier.padding(top = 20.dp),
        )

        AppTextField(
            label = "Senha",
            value = password,
            onChange = { input -> password = input },
            placeholder = "Crie uma senha forte",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            cornerRadius = 16.dp,
            modifier = Modifier.padding(top = 20.dp),
        )

        GradientSubmitButton(
            label = "Criar Conta",
            isSending = state.saving,
            onClick = { viewModel.register(name, email, password) },
            modifier = Modifier.padding(top = 32.dp),
        )

        AuthFooter(
            text = "Já tem uma conta? ",
            link = "Faça Login",
            onClick = onLogin,
            modifier = Modifier.padding(top = 32.dp),
        )
    }
}
