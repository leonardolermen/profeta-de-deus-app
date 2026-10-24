package br.org.profetas.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppContainer
import br.org.profetas.app.bible.BibleScreen
import br.org.profetas.app.bible.BibleViewModel
import br.org.profetas.app.study.StudiesScreen
import br.org.profetas.app.study.StudiesViewModel
import br.org.profetas.app.study.StudyScreen

// Aba Bíblia e os estudos que se abrem por ela. Favoritos e estudos concluídos são guardados por conta: ao entrar ou
// sair da conta, as telas releem o que pertence ao novo dono.
fun NavGraphBuilder.bibleDestinations(
    container: AppContainer,
    bibleViewModel: BibleViewModel,
    studiesViewModel: StudiesViewModel,
    navigator: Navigator,
) {
    composable(Route.Bible.pattern) {
        val isLoggedIn by container.account.isLoggedIn.collectAsStateWithLifecycle()

        LaunchedEffect(isLoggedIn) {
            bibleViewModel.refreshFavorites()
        }

        BibleScreen(viewModel = bibleViewModel, onNavigate = navigator::go)
    }

    composable(Route.Studies.pattern) {
        val isLoggedIn by container.account.isLoggedIn.collectAsStateWithLifecycle()

        LaunchedEffect(isLoggedIn) {
            studiesViewModel.refreshCompleted()
        }

        StudiesScreen(viewModel = studiesViewModel, onNavigate = navigator::go)
    }

    composable(Route.Study.PATTERN) { entry ->
        val isLoggedIn by container.account.isLoggedIn.collectAsStateWithLifecycle()

        LaunchedEffect(isLoggedIn) {
            studiesViewModel.refreshCompleted()
        }

        StudyScreen(studyId = entry.arguments?.getString("id").orEmpty(), viewModel = studiesViewModel)
    }
}
