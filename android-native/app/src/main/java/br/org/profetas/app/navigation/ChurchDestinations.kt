package br.org.profetas.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.church.ChurchViewModel
import br.org.profetas.app.church.ContactScreen
import br.org.profetas.app.church.HistoryScreen
import br.org.profetas.app.church.PrivacyScreen

// História, Contato e Privacidade: três telas sobre os dados da mesma igreja, que o ChurchViewModel já carrega ao
// abrir o app e recarrega no puxar-para-atualizar.
fun NavGraphBuilder.churchDestinations(churchViewModel: ChurchViewModel) {
    composable(Route.History.pattern) {
        HistoryScreen(viewModel = churchViewModel)
    }

    composable(Route.Contact.pattern) {
        ContactScreen(viewModel = churchViewModel)
    }

    composable(Route.Privacy.pattern) {
        PrivacyScreen(viewModel = churchViewModel)
    }
}
