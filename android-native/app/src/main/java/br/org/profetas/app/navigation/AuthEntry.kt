package br.org.profetas.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import br.org.profetas.app.account.*
import br.org.profetas.app.theme.ProfetasTheme

/** Grafo sem abas ou conteúdo privado: Voltar e links não abrem a área de membros. */
@Composable
internal fun AuthEntry(account: AccountViewModel, isDark: Boolean) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(account) { account.userMessages.collect { snackbar.showSnackbar(it) } }
    val entry by nav.currentBackStackEntryAsState()
    val visible = entry?.destination?.route ?: Route.Login.path
    ProfetasTheme(isDark = isDark) {
        Scaffold(
            topBar = {
                if (visible != Route.Login.path) TopBar(Route.fromPath(visible) ?: Route.Login) { nav.popBackStack() }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            NavHost(nav, startDestination = Route.Login.path, modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
                composable(Route.Login.path) {
                    LoginScreen(account, onSignedIn = {}, onRegister = { nav.navigate(Route.Register.path) }, onRecovery = { nav.navigate(Route.Recovery.path) })
                }
                composable(Route.Register.path) {
                    RegisterScreen(account, onSignedIn = { nav.popBackStack(Route.Login.path, false) }, onLogin = { nav.popBackStack(Route.Login.path, false) })
                }
                composable(Route.Recovery.path) {
                    RecoveryScreen(account, onLogin = { nav.popBackStack(Route.Login.path, false) })
                }
            }
        }
    }
}
