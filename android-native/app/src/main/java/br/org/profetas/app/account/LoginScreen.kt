package br.org.profetas.app.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import br.org.profetas.app.support.ui.PasswordField
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.ui.AppTextField

// app/auth/login.tsx. "Esqueci minha senha" não existe no Expo: é a porta para a tela de recuperação.
@Composable
fun LoginScreen(
    viewModel: AccountViewModel,
    onSignedIn: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            emoji = "👋",
            title = "Bem-vindo de volta!",
            subtitle = "Entre com sua conta para acessar o aplicativo da igreja. Confirme seu e-mail após o cadastro.",
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

        PasswordField(
            label = "Senha",
            value = password,
            onChange = { input -> password = input },
            placeholder = "Sua senha secreta",
            cornerRadius = 16.dp,
            modifier = Modifier.padding(top = 20.dp),
        )

        GradientSubmitButton(
            label = "Entrar",
            isSending = state.saving,
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.padding(top = 32.dp),
        )

        Text(
            text = "Esqueci minha senha",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable(role = Role.Button, onClick = onRecovery),
        )

        AuthFooter(
            text = "Ainda não tem conta? ",
            link = "Cadastre-se",
            onClick = onRegister,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}
