package br.org.profetas.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppContainer
import br.org.profetas.app.account.AccountViewModel
import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.admin.AdminGate
import br.org.profetas.app.admin.AdminScreen
import br.org.profetas.app.admin.AdminViewModel
import br.org.profetas.app.admin.BirthdayEditor
import br.org.profetas.app.admin.ChurchEditor
import br.org.profetas.app.admin.EventEditor
import br.org.profetas.app.admin.PhotoEditor
import br.org.profetas.app.admin.StudyEditor
import br.org.profetas.app.admin.WeeklyServiceEditor

// Administração e seus editores. Toda tela passa pelo AdminGate e carrega os cadastros na primeira visita de um
// admin; os editores voltam para a lista quando a gravação termina.
fun NavGraphBuilder.adminDestinations(
    container: AppContainer,
    adminViewModel: AdminViewModel,
    accountViewModel: AccountViewModel,
    navigator: Navigator,
) {
    val baseUrl = container.storageBaseUrl
    val back = navigator::back

    admin(Route.Admin.pattern, container, adminViewModel, accountViewModel) {
        AdminScreen(viewModel = adminViewModel, onNavigate = navigator::go)
    }

    admin(Route.EventEditor.PATTERN, container, adminViewModel, accountViewModel) { entry ->
        EventEditor(entry.recordId(), adminViewModel, baseUrl, back)
    }

    admin(Route.WeeklyServiceEditor.PATTERN, container, adminViewModel, accountViewModel) { entry ->
        WeeklyServiceEditor(entry.recordId(), adminViewModel, back)
    }

    admin(Route.StudyEditor.PATTERN, container, adminViewModel, accountViewModel) { entry ->
        StudyEditor(entry.recordId(), adminViewModel, back)
    }

    admin(Route.PhotoEditor.PATTERN, container, adminViewModel, accountViewModel) { entry ->
        PhotoEditor(entry.recordId(), adminViewModel, baseUrl, back)
    }

    admin(Route.BirthdayEditor.PATTERN, container, adminViewModel, accountViewModel) { entry ->
        BirthdayEditor(entry.recordId(), adminViewModel, back)
    }

    admin(Route.ChurchEditor.pattern, container, adminViewModel, accountViewModel) {
        ChurchEditor(adminViewModel, back)
    }
}

private fun NavGraphBuilder.admin(
    pattern: String,
    container: AppContainer,
    adminViewModel: AdminViewModel,
    accountViewModel: AccountViewModel,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(pattern) { entry ->
        val profile by container.account.currentProfile.collectAsStateWithLifecycle()
        val isLoggedIn by container.account.isLoggedIn.collectAsStateWithLifecycle()
        val isAdmin = ContentAccess.canManage(profile)

        // O perfil só existe depois de carregado; aberta direto, a tela pede a carga em vez de recusar o acesso.
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn && profile == null) {
                accountViewModel.loadProfile()
            }
        }

        LaunchedEffect(isAdmin) {
            if (isAdmin) {
                adminViewModel.loadIfEmpty()
            }
        }

        AdminGate(profile = profile, isLoggedIn = isLoggedIn) {
            content(entry)
        }
    }
}

private fun NavBackStackEntry.recordId(): String = arguments?.getString("id").orEmpty()
