package br.org.profetas.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import br.org.profetas.app.AppContainer
import br.org.profetas.app.gallery.GalleryScreen
import br.org.profetas.app.gallery.GalleryViewModel
import br.org.profetas.app.gallery.PhotoScreen

// Galeria e a foto aberta por ela. As fotos são públicas, então carregam na primeira visita sem esperar a conta.
fun NavGraphBuilder.galleryDestinations(
    container: AppContainer,
    galleryViewModel: GalleryViewModel,
    navigator: Navigator,
) {
    composable(Route.Gallery.pattern) {
        LaunchedEffect(Unit) {
            galleryViewModel.loadIfEmpty()
        }

        GalleryScreen(
            viewModel = galleryViewModel,
            storageBaseUrl = container.storageBaseUrl,
            onNavigate = navigator::go,
        )
    }

    composable(Route.Photo.PATTERN) { entry ->
        LaunchedEffect(Unit) {
            galleryViewModel.loadIfEmpty()
        }

        PhotoScreen(
            photoId = entry.arguments?.getString("id").orEmpty(),
            viewModel = galleryViewModel,
            storageBaseUrl = container.storageBaseUrl,
        )
    }
}
