package br.org.profetas.app.church

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

data class ChurchState(
    val loading: Boolean = false,
    val failure: String? = null,
    val church: Church = Church(),
) {
    // Durante a carga, "ainda não foi publicada" seria um vazio falso: o texto fica em branco até a resposta chegar.
    val historyPreview: String
        get() = when {
            church.history.isNotBlank() -> church.history
            loading -> ""
            else -> "A história da comunidade ainda não foi publicada."
        }
}

class ChurchViewModel(private val churches: ChurchRepository) : ViewModel() {

    private val mutableState = MutableStateFlow(ChurchState())

    val state: StateFlow<ChurchState> = mutableState.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        if (state.value.loading) {
            return
        }

        mutableState.update { current -> current.copy(loading = true, failure = null) }

        viewModelScope.launch {
            try {
                val details = churches.details()
                mutableState.update { current -> current.copy(loading = false, church = details) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                val message = e.message ?: "Não foi possível carregar as informações da igreja."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                ChurchViewModel(application.container.church)
            }
        }
    }
}
