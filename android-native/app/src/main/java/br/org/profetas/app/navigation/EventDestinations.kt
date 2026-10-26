package br.org.profetas.app.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppContainer
import br.org.profetas.app.baptism.BaptismScreen
import br.org.profetas.app.campaign.CampaignsScreen
import br.org.profetas.app.event.EventDetailScreen
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.event.registration.MyRegistrationsScreen

// Telas empilhadas que leem a agenda do EventsViewModel: detalhe, inscrições, Projetos e Batismo.
fun NavGraphBuilder.eventDestinations(container: AppContainer, eventsViewModel: EventsViewModel, navigator: Navigator) {
    composable(Route.EventDetail.PATTERN) { entry ->
        val accountId by container.account.accountId.collectAsStateWithLifecycle()
        val profile by container.account.currentProfile.collectAsStateWithLifecycle()

        EventDetailScreen(
            eventId = entry.arguments?.getString("id").orEmpty(),
            viewModel = eventsViewModel,
            accountId = accountId,
            profileName = profile?.name,
            storageBaseUrl = container.storageBaseUrl,
            userMessages = container.userMessages,
            onNavigate = navigator::go,
        )
    }

    composable(Route.MyRegistrations.pattern) {
        val accountId by container.account.accountId.collectAsStateWithLifecycle()

        MyRegistrationsScreen(viewModel = eventsViewModel, accountId = accountId, onNavigate = navigator::go)
    }

    composable(Route.Campaigns.pattern) {
        CampaignsScreen(viewModel = eventsViewModel, onNavigate = navigator::go)
    }

    composable(Route.Baptism.pattern) {
        BaptismScreen(viewModel = eventsViewModel, onNavigate = navigator::go)
    }
}
