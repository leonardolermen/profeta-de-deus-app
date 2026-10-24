package br.org.profetas.app.church

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// O Expo não tem tela de privacidade: ela segue o visual da História e mostra o texto que a igreja cadastrou.
@Composable
fun PrivacyScreen(viewModel: ChurchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChurchTextPage(
        state = state,
        title = "Privacidade",
        text = state.church.privacy,
        whenBlank = "A política de privacidade ainda não foi publicada pela igreja. Entre em contato com a " +
            "secretaria para informações sobre o tratamento dos seus dados.",
        onRetry = viewModel::reload,
    )
}
