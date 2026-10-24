package br.org.profetas.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.org.profetas.app.AppContainer
import br.org.profetas.app.AppViewModel
import br.org.profetas.app.church.ChurchViewModel
import br.org.profetas.app.event.EventsScreen
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.home.HomeScreen
import br.org.profetas.app.theme.ProfetasTheme

// O que o puxar-para-atualizar precisa da tela visível: o que recarregar e se há carga em curso.
private class VisibleScreen(val reload: () -> Unit, val isBusy: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfetasApp(container: AppContainer, initialRoute: Route?, recoveryLink: String?) {
    val appViewModel: AppViewModel = viewModel()
    val eventsViewModel: EventsViewModel = viewModel(factory = EventsViewModel.Factory)
    val churchViewModel: ChurchViewModel = viewModel(factory = ChurchViewModel.Factory)

    val appState by appViewModel.state.collectAsStateWithLifecycle()
    val eventsState by eventsViewModel.state.collectAsStateWithLifecycle()
    val churchState by churchViewModel.state.collectAsStateWithLifecycle()
    val profile by container.account.currentProfile.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navigator = remember(navController) { Navigator(navController) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    // O molde do destino ("event/{id}") vira uma rota com argumentos de mentira, o bastante para título e aba.
    val visible = backStackEntry?.destination?.route?.let(Route::fromPath) ?: Route.Home

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(appState.message) {
        appState.message?.let { message ->
            snackbarHost.showSnackbar(message)
            appViewModel.dismissMessage()
        }
    }

    LaunchedEffect(Unit) {
        container.userMessages.messages.collect { message ->
            snackbarHost.showSnackbar(message)
        }
    }

    LaunchedEffect(initialRoute) {
        initialRoute?.takeIf { route -> route != Route.Home }?.let(navigator::go)
    }

    LaunchedEffect(recoveryLink) {
        recoveryLink?.let { link ->
            appViewModel.action("Defina sua nova senha no perfil.", { navigator.go(Route.Profile) }) {
                account.acceptRecovery(link)
            }
        }
    }

    val screen = when (visible) {
        Route.Home -> VisibleScreen(
            reload = {
                eventsViewModel.reload()
                churchViewModel.reload()
            },
            isBusy = eventsState.loading || churchState.loading,
        )
        Route.Events, Route.Campaigns, Route.Baptism ->
            VisibleScreen(reload = eventsViewModel::reload, isBusy = eventsState.loading)
        is Route.EventDetail, Route.MyRegistrations -> VisibleScreen(
            reload = {
                eventsViewModel.reload()
                eventsViewModel.reloadRegistrations()
            },
            isBusy = eventsState.loading,
        )
        else -> VisibleScreen(reload = appViewModel::reload, isBusy = appState.loading || appState.saving)
    }

    ProfetasTheme(isDark = appState.dark) {
        Scaffold(
            topBar = {
                if (visible !in Route.tabs) {
                    TopBar(visible = visible, onBack = navigator::back)
                }
            },
            bottomBar = {
                if (visible in Route.tabs) {
                    TabBar(selected = visible, onSelect = navigator::go)
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHost)
            },
        ) { padding ->
            // Sem barra superior nas abas, o padding do Scaffold já traz o recuo da barra de status. O NavHost fica
            // sempre dentro do PullToRefreshBox: trocar o pai entre aba e tela empilhada recriaria a navegação.
            PullToRefreshBox(
                isRefreshing = screen.isBusy,
                onRefresh = screen.reload,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Route.Home.pattern,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(Route.Home.pattern) {
                        HomeScreen(
                            eventsViewModel = eventsViewModel,
                            churchViewModel = churchViewModel,
                            greetingName = profile?.name,
                            storageBaseUrl = container.storageBaseUrl,
                            onNavigate = navigator::go,
                        )
                    }

                    composable(Route.Events.pattern) {
                        EventsScreen(
                            viewModel = eventsViewModel,
                            storageBaseUrl = container.storageBaseUrl,
                            onNavigate = navigator::go,
                        )
                    }

                    eventDestinations(container, eventsViewModel, navigator)

                    legacyDestinations(appViewModel, navigator)
                }
            }
        }
    }
}
