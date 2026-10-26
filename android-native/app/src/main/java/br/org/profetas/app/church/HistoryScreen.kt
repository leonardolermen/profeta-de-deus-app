package br.org.profetas.app.church

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// O Expo tinha o texto e os pastores fixos no código; aqui o texto vem da igreja cadastrada e não há pastor inventado.
@Composable
fun HistoryScreen(viewModel: ChurchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChurchTextPage(
        state = state,
        title = "Quem Somos",
        text = state.church.history,
        whenBlank = "A história da comunidade ainda não foi publicada.",
        onRetry = viewModel::reload,
    )
}
