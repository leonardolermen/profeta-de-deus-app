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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.org.profetas.app.AppContainer
import br.org.profetas.app.account.AccountEvent
import br.org.profetas.app.account.AccountViewModel
import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.admin.AdminViewModel
import br.org.profetas.app.bible.BibleViewModel
import br.org.profetas.app.birthday.BirthdaysViewModel
import br.org.profetas.app.church.ChurchViewModel
import br.org.profetas.app.event.EventsScreen
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.gallery.GalleryViewModel
import br.org.profetas.app.home.HomeScreen
import br.org.profetas.app.prayer.PrayerViewModel
import br.org.profetas.app.settings.SettingsViewModel
import br.org.profetas.app.study.StudiesViewModel
import br.org.profetas.app.theme.ProfetasTheme

// O que o puxar-para-atualizar precisa da tela visível: o que recarregar e se há carga em curso.
private class VisibleScreen(val reload: () -> Unit, val isBusy: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfetasApp(container: AppContainer, initialRoute: Route?, recoveryLink: String?) {
    val accountViewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val accountId by container.account.accountId.collectAsStateWithLifecycle()
    val isDark by settingsViewModel.isDark.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(accountId) { context.imageLoader.memoryCache?.clear() }

    LaunchedEffect(recoveryLink) { recoveryLink?.let(accountViewModel::acceptRecovery) }

    if (accountId == null) {
        AuthEntry(accountViewModel, isDark)
        return
    }

    // Dados e navegação de uma conta não sobrevivem ao logout ou à troca de usuário.
    val parent = checkNotNull(LocalViewModelStoreOwner.current) as HasDefaultViewModelProviderFactory
    val owner = remember(accountId, parent) {
        object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
            override val viewModelStore = ViewModelStore()
            override val defaultViewModelProviderFactory = parent.defaultViewModelProviderFactory
            override val defaultViewModelCreationExtras = parent.defaultViewModelCreationExtras
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        MemberApp(container, if (recoveryLink != null) Route.Profile else initialRoute, accountViewModel, settingsViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberApp(container: AppContainer, initialRoute: Route?, accountViewModel: AccountViewModel, settingsViewModel: SettingsViewModel) {
    val eventsViewModel: EventsViewModel = viewModel(factory = EventsViewModel.Factory)
    val churchViewModel: ChurchViewModel = viewModel(factory = ChurchViewModel.Factory)
    val bibleViewModel: BibleViewModel = viewModel(factory = BibleViewModel.Factory)
    val studiesViewModel: StudiesViewModel = viewModel(factory = StudiesViewModel.Factory)
    val prayerViewModel: PrayerViewModel = viewModel(factory = PrayerViewModel.Factory)
    val birthdaysViewModel: BirthdaysViewModel = viewModel(factory = BirthdaysViewModel.Factory)
    val galleryViewModel: GalleryViewModel = viewModel(factory = GalleryViewModel.Factory)
    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory)

    val eventsState by eventsViewModel.state.collectAsStateWithLifecycle()
    val churchState by churchViewModel.state.collectAsStateWithLifecycle()
    val bibleState by bibleViewModel.state.collectAsStateWithLifecycle()
    val studiesState by studiesViewModel.state.collectAsStateWithLifecycle()
    val birthdaysState by birthdaysViewModel.state.collectAsStateWithLifecycle()
    val galleryState by galleryViewModel.state.collectAsStateWithLifecycle()
    val adminState by adminViewModel.state.collectAsStateWithLifecycle()
    val profile by container.account.currentProfile.collectAsStateWithLifecycle()
    val isDark by settingsViewModel.isDark.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navigator = remember(navController) { Navigator(navController) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    // O molde do destino ("event/{id}") vira uma rota com argumentos de mentira, o bastante para título e aba.
    val visible = backStackEntry?.destination?.route?.let(Route::fromPath) ?: Route.Home

    val snackbarHost = remember { SnackbarHostState() }
    val accountId by container.account.accountId.collectAsStateWithLifecycle()
    LaunchedEffect(accountId) { accountViewModel.loadProfile() }

    LaunchedEffect(Unit) {
        container.userMessages.messages.collect { message ->
            snackbarHost.showSnackbar(message)
        }
    }

    LaunchedEffect(initialRoute) {
        initialRoute?.takeIf { route -> route != Route.Home }?.let(navigator::go)
    }

    LaunchedEffect(Unit) {
        accountViewModel.events.collect { event ->
            if (event == AccountEvent.RECOVERY_ACCEPTED) {
                navigator.go(Route.Profile)
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
        Route.Bible -> VisibleScreen(reload = bibleViewModel::reload, isBusy = bibleState.isBusy)
        Route.Studies, is Route.Study ->
            VisibleScreen(reload = studiesViewModel::reload, isBusy = studiesState.loading)
        Route.Birthdays -> VisibleScreen(reload = birthdaysViewModel::reload, isBusy = birthdaysState.loading)
        Route.Gallery, is Route.Photo ->
            VisibleScreen(reload = galleryViewModel::reload, isBusy = galleryState.loading)
        Route.Profile -> VisibleScreen(reload = accountViewModel::loadProfile, isBusy = false)
        Route.History, Route.Contact, Route.Privacy ->
            VisibleScreen(reload = churchViewModel::reload, isBusy = churchState.loading)
        Route.Admin, is Route.EventEditor, is Route.WeeklyServiceEditor, is Route.StudyEditor, is Route.PhotoEditor,
        is Route.BirthdayEditor, Route.ChurchEditor -> VisibleScreen(
            reload = {
                // Sem perfil a administração nem abre: atualizar também refaz a carga dele, que pode ter falhado.
                if (profile == null) {
                    accountViewModel.loadProfile()
                }

                adminViewModel.reload()
            },
            isBusy = adminState.loading,
        )
        Route.Settings -> VisibleScreen(
            reload = {
                settingsViewModel.reload()
                accountViewModel.loadProfile()
            },
            isBusy = false,
        )
        // Formulários sem lista remota para atualizar: o que digitar é enviado, não carregado. Sem `else` de propósito,
        // para o compilador exigir uma decisão explícita a cada rota nova.
        Route.Prayer, Route.Login, Route.Register, Route.Recovery -> VisibleScreen(reload = {}, isBusy = false)
    }

    ProfetasTheme(isDark = isDark) {
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
                            canManage = ContentAccess.canManage(profile),
                        )
                    }

                    eventDestinations(container, eventsViewModel, navigator)

                    bibleDestinations(container, bibleViewModel, studiesViewModel, navigator)

                    communityDestinations(container, prayerViewModel, birthdaysViewModel, navigator)

                    galleryDestinations(container, galleryViewModel, navigator)

                    churchDestinations(churchViewModel)

                    accountDestinations(accountViewModel, navigator)

                    adminDestinations(container, adminViewModel, accountViewModel, navigator, eventsViewModel::reload)

                    settingsDestinations(settingsViewModel, accountViewModel, navigator)
                }
            }
        }
    }
}
