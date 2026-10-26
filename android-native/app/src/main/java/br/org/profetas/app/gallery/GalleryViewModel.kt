package br.org.profetas.app.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryState(
    val loading: Boolean = false,
    val failure: String? = null,
    val photos: List<Photo> = emptyList(),
)

class GalleryViewModel(private val gallery: GalleryRepository) : ViewModel() {

    private val mutableState = MutableStateFlow(GalleryState())

    val state: StateFlow<GalleryState> = mutableState.asStateFlow()

    fun loadIfEmpty() {
        if (state.value.photos.isEmpty()) {
            reload()
        }
    }

    fun reload() {
        if (state.value.loading) {
            return
        }

        mutableState.update { current -> current.copy(loading = true, failure = null) }

        viewModelScope.launch {
            try {
                val list = gallery.all()

                mutableState.update { current -> current.copy(loading = false, photos = list) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                // Mantém as fotos anteriores: sem rede, quem já abriu a galeria continua vendo o que carregou.
                val message = e.message ?: "Não foi possível carregar a galeria."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                GalleryViewModel(gallery = application.container.gallery)
            }
        }
    }
}
