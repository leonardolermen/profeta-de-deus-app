package br.org.profetas.app.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppState
import br.org.profetas.app.AppViewModel
import br.org.profetas.app.ui.AdminScreen
import br.org.profetas.app.ui.AuthScreen
import br.org.profetas.app.ui.BirthdaysScreen
import br.org.profetas.app.ui.ContactScreen
import br.org.profetas.app.ui.EditorScreen
import br.org.profetas.app.ui.Empty
import br.org.profetas.app.ui.GalleryScreen
import br.org.profetas.app.ui.Heading
import br.org.profetas.app.ui.LoadStatus
import br.org.profetas.app.ui.Page
import br.org.profetas.app.ui.PhotoScreen
import br.org.profetas.app.ui.PrayerScreen
import br.org.profetas.app.ui.ProfileScreen
import br.org.profetas.app.ui.SettingsScreen

// Telas ainda não convertidas: continuam nos composables antigos de ui/ com o AppViewModel. Cada task de tela
// tira daqui o destino que ela converte.
fun NavGraphBuilder.legacyDestinations(appViewModel: AppViewModel, navigator: Navigator) {
    val goPath = navigator::goLegacyPath
    val back = navigator::back

    legacy(Route.Settings, appViewModel) { state -> SettingsScreen(state, appViewModel, goPath) }
    legacy(Route.Prayer, appViewModel) { state -> PrayerScreen(state, appViewModel, back) }
    legacy(Route.Birthdays, appViewModel) { state -> BirthdaysScreen(state, appViewModel, goPath) }
    legacy(Route.Gallery, appViewModel) { state -> GalleryScreen(state, appViewModel, goPath) }
    legacy(Route.Contact, appViewModel) { state -> ContactScreen(state, appViewModel) }
    legacy(Route.Profile, appViewModel) { state -> ProfileScreen(state, appViewModel) }
    legacy(Route.Admin, appViewModel) { state -> AdminScreen(state, appViewModel, goPath) }
    legacy(Route.History, appViewModel) { state -> HistoryPage(state, appViewModel) }
    legacy(Route.Privacy, appViewModel) { state -> PrivacyPage(state, appViewModel) }

    // A tela antiga de conta faz entrar, cadastrar e recuperar numa só; as três rotas abrem ela até a Task 14.
    listOf(Route.Login, Route.Register, Route.Recovery).forEach { route ->
        legacy(route, appViewModel) { state -> AuthScreen(state, appViewModel, back) }
    }

    legacy(Route.Photo.PATTERN, appViewModel) { state, entry ->
        PhotoScreen(state, appViewModel.storageBaseUrl, entry.argument("id"))
    }
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

@Composable
private fun HistoryPage(state: AppState, appViewModel: AppViewModel) = Page {
    Heading("Nossa história")
    LoadStatus(state, "Igreja", appViewModel::reload)

    if (state.church.history.isBlank()) {
        Empty("A história da comunidade ainda não foi publicada.")
    } else {
        Text(state.church.history)
    }
}

@Composable
private fun PrivacyPage(state: AppState, appViewModel: AppViewModel) = Page {
    Heading("Privacidade")
    LoadStatus(state, "Igreja", appViewModel::reload)

    Text(
        state.church.privacy.ifBlank {
            "A política de privacidade ainda não foi publicada pela igreja. Entre em contato com a secretaria " +
                "para informações sobre o tratamento dos seus dados."
        },
    )
}

