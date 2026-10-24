package br.org.profetas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import br.org.profetas.app.AppViewModel
import br.org.profetas.app.theme.ProfetasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProfetasApp(vm: AppViewModel, initialEvent: String?, recovery: String?) {
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()
    val route = current?.destination?.route ?: "home"
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); vm.dismissMessage() } }
    LaunchedEffect(initialEvent) { initialEvent?.let { nav.navigate("event/$it") } }
    LaunchedEffect(recovery) { recovery?.let { link -> vm.action("Defina sua nova senha no perfil.", {nav.navigate("profile")}) {acceptRecovery(link)} } }
    val roots = listOf("home", "bible", "events", "community", "settings")
    fun go(path: String) { nav.navigate(path) { launchSingleTop=true } }
    ProfetasTheme(isDark=state.dark) {
        Scaffold(
            topBar={ TopAppBar(title={ Text("Profetas de Deus") }, navigationIcon={ if(route !in roots) IconButton(onClick={ nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack,"Voltar") } }, actions={ IconButton(onClick=vm::reload, enabled=!state.loading) { Icon(Icons.Default.Refresh,"Atualizar dados") } }) },
            bottomBar={ if(route in roots) NavigationBar {
                val labels=listOf("Início","Bíblia","Agenda","Comunidade","Ajustes")
                val icons=listOf(Icons.Default.Home,Icons.AutoMirrored.Filled.MenuBook,Icons.Default.Event,Icons.Default.Groups,Icons.Default.Settings)
                roots.forEachIndexed { index,path -> NavigationBarItem(selected=route==path, onClick={ nav.navigate(path) { popUpTo("home") { saveState=true }; launchSingleTop=true; restoreState=true } }, icon={ Icon(icons[index],labels[index]) },label={ Text(labels[index]) }) }
            } },
            snackbarHost={ SnackbarHost(snackbar) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
                if(state.loading || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                NavHost(navController=nav,startDestination="home",modifier=Modifier.weight(1f)) {
                    composable("home") { HomeScreen(state,vm.configured,::go) }
                    composable("bible") { BibleScreen(state,vm) }
                    composable("events") { EventsScreen(state,vm,::go) }
                    composable("community") { CommunityScreen(::go) }
                    composable("settings") { SettingsScreen(state,vm,::go) }
                    composable("auth") { AuthScreen(state,vm) { nav.popBackStack() } }
                    composable("profile") { ProfileScreen(state,vm) }
                    composable("prayer") { PrayerScreen(state,vm) { nav.popBackStack() } }
                    composable("studies") { StudiesScreen(state,vm,::go) }
                    composable("study/{id}",arguments=listOf(navArgument("id"){type=NavType.StringType})) { StudyScreen(state,vm,it.arguments?.getString("id").orEmpty()) }
                    composable("birthdays") { BirthdaysScreen(state,vm,::go) }
                    composable("gallery") { GalleryScreen(state,vm,::go) }
                    composable("photo/{id}",arguments=listOf(navArgument("id"){type=NavType.StringType})) { PhotoScreen(state,it.arguments?.getString("id").orEmpty()) }
                    composable("contact") { ContactScreen(state,vm) }
                    composable("history") { Page { Heading("Nossa história"); LoadStatus(state,"Igreja",vm::reload); if(state.church.history.isBlank()) Empty("A história da comunidade ainda não foi publicada.") else Text(state.church.history) } }
                    composable("privacy") { Page { Heading("Privacidade"); LoadStatus(state,"Igreja",vm::reload); Text(state.church.privacy.ifBlank { "A política de privacidade ainda não foi publicada pela igreja. Entre em contato com a secretaria para informações sobre o tratamento dos seus dados." }) } }
                    composable("event/{id}",arguments=listOf(navArgument("id"){type=NavType.StringType})) { EventScreen(state,vm,it.arguments?.getString("id").orEmpty(),::go) }
                    composable("registrations") { RegistrationsScreen(state,vm,::go) }
                    composable("admin") { AdminScreen(state,vm,::go) }
                    composable("editor/{table}/{id}",arguments=listOf(navArgument("table"){type=NavType.StringType},navArgument("id"){type=NavType.StringType})) { EditorScreen(state,vm,it.arguments?.getString("table").orEmpty(),it.arguments?.getString("id").orEmpty()) { nav.popBackStack() } }
                }
            }
        }
    }
}
