package br.org.profetas.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppState
import br.org.profetas.app.AppViewModel
import br.org.profetas.app.ui.AdminScreen
import br.org.profetas.app.ui.EditorScreen
import br.org.profetas.app.ui.SettingsScreen

// Telas ainda não convertidas: continuam nos composables antigos de ui/ com o AppViewModel. Cada task de tela
// tira daqui o destino que ela converte.
fun NavGraphBuilder.legacyDestinations(appViewModel: AppViewModel, navigator: Navigator) {
    val goPath = navigator::goLegacyPath
    val back = navigator::back

    legacy(Route.Settings, appViewModel) { state -> SettingsScreen(state, appViewModel, goPath) }
    legacy(Route.Admin, appViewModel) { state -> AdminScreen(state, appViewModel, goPath) }

    legacy(Route.Editor.PATTERN, appViewModel) { state, entry ->
        EditorScreen(state, appViewModel, entry.argument("section"), entry.argument("id"), back)
    }
}

private fun NavGraphBuilder.legacy(
    route: Route,
    appViewModel: AppViewModel,
    content: @Composable (AppState) -> Unit,
) {
    legacy(route.pattern, appViewModel) { state, _ -> content(state) }
}

private fun NavGraphBuilder.legacy(
    pattern: String,
    appViewModel: AppViewModel,
    content: @Composable (AppState, NavBackStackEntry) -> Unit,
) {
    composable(pattern) { entry ->
        val state by appViewModel.state.collectAsStateWithLifecycle()

        content(state, entry)
    }
}

private fun NavBackStackEntry.argument(name: String): String = arguments?.getString(name).orEmpty()
