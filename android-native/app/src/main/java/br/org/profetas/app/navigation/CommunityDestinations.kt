package br.org.profetas.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppContainer
import br.org.profetas.app.birthday.BirthdaysScreen
import br.org.profetas.app.birthday.BirthdaysViewModel
import br.org.profetas.app.prayer.PrayerScreen
import br.org.profetas.app.prayer.PrayerViewModel

// Oração e Aniversariantes, alcançadas pelos atalhos da Início. Oração é só formulário, sem dado para recarregar;
// Aniversariantes recarrega ao entrar ou sair da conta, porque a RLS só devolve linhas para quem está logado.
fun NavGraphBuilder.communityDestinations(
    container: AppContainer,
    prayerViewModel: PrayerViewModel,
    birthdaysViewModel: BirthdaysViewModel,
    navigator: Navigator,
) {
    composable(Route.Prayer.pattern) {
        PrayerScreen(viewModel = prayerViewModel, onBack = navigator::back)
    }

    composable(Route.Birthdays.pattern) {
        val isLoggedIn by container.account.isLoggedIn.collectAsStateWithLifecycle()

        LaunchedEffect(isLoggedIn) {
            birthdaysViewModel.reload()
        }

        BirthdaysScreen(viewModel = birthdaysViewModel, isLoggedIn = isLoggedIn, onNavigate = navigator::go)
    }
}
