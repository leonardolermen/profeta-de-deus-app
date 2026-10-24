package br.org.profetas.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.account.AccountViewModel
import br.org.profetas.app.account.LoginScreen
import br.org.profetas.app.account.ProfileScreen
import br.org.profetas.app.account.RecoveryScreen
import br.org.profetas.app.account.RegisterScreen

// Login, cadastro, recuperação e perfil compartilham o AccountViewModel, que o ProfetasApp também usa para aceitar o
// link de recuperação.
fun NavGraphBuilder.accountDestinations(accountViewModel: AccountViewModel, navigator: Navigator) {
    composable(Route.Login.pattern) {
        LoginScreen(
            viewModel = accountViewModel,
            onSignedIn = navigator::finishSignIn,
            onRegister = { navigator.go(Route.Register) },
            onRecovery = { navigator.go(Route.Recovery) },
        )
    }

    composable(Route.Register.pattern) {
        RegisterScreen(
            viewModel = accountViewModel,
            onSignedIn = navigator::finishSignIn,
            onLogin = navigator::back,
        )
    }

    composable(Route.Recovery.pattern) {
        RecoveryScreen(viewModel = accountViewModel, onLogin = navigator::back)
    }

    composable(Route.Profile.pattern) {
        ProfileScreen(viewModel = accountViewModel)
    }
}
