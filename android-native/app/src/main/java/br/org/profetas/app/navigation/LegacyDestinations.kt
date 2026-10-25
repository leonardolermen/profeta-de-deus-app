package br.org.profetas.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppState
import br.org.profetas.app.AppViewModel
import br.org.profetas.app.ui.SettingsScreen

// Telas ainda não convertidas: continuam nos composables antigos de ui/ com o AppViewModel. Cada task de tela
// tira daqui o destino que ela converte.
fun NavGraphBuilder.legacyDestinations(appViewModel: AppViewModel, navigator: Navigator) {
    val goPath = navigator::goLegacyPath

    legacy(Route.Settings, appViewModel) { state -> SettingsScreen(state, appViewModel, goPath) }
}

private fun NavGraphBuilder.legacy(
    route: Route,
    appViewModel: AppViewModel,
    content: @Composable (AppState) -> Unit,
) {
    composable(route.pattern) {
        val state by appViewModel.state.collectAsStateWithLifecycle()

        content(state)
    }
}
