package br.org.profetas.app.study

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

data class StudiesState(
    val loading: Boolean = false,
    val failure: String? = null,
    val studies: List<Study> = emptyList(),
    val completedIds: Set<String> = emptySet(),
)

class StudiesViewModel(
    private val studies: StudyRepository,
    private val completed: CompletedStudies,
) : ViewModel() {

    private val mutableState = MutableStateFlow(StudiesState(completedIds = completed.all()))

    val state: StateFlow<StudiesState> = mutableState.asStateFlow()

    fun loadIfEmpty() {
        if (state.value.studies.isEmpty()) {
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
                val list = studies.all()

                mutableState.update { current -> current.copy(loading = false, studies = list) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                // Mantém a lista anterior: sem rede, quem estava lendo um estudo continua com ele na tela.
                val message = e.message ?: "Não foi possível carregar os estudos."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
    }

    fun toggleCompleted(id: String) {
        completed.toggle(id)

        refreshCompleted()
    }

    // As conclusões são por conta: quem entra ou sai da conta passa a ver as do novo dono.
    fun refreshCompleted() {
        mutableState.update { current -> current.copy(completedIds = completed.all()) }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                StudiesViewModel(studies = container.studies, completed = container.completedStudies)
            }
        }
    }
}
