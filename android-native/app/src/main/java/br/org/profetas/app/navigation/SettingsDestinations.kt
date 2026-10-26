package br.org.profetas.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.account.AccountViewModel
import br.org.profetas.app.settings.SettingsScreen
import br.org.profetas.app.settings.SettingsViewModel

fun NavGraphBuilder.settingsDestinations(
    settingsViewModel: SettingsViewModel,
    accountViewModel: AccountViewModel,
    navigator: Navigator,
) {
    composable(Route.Settings.pattern) {
        SettingsScreen(
            settingsViewModel = settingsViewModel,
            accountViewModel = accountViewModel,
            onNavigate = navigator::go,
        )
    }
}
