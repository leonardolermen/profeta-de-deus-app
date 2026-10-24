package br.org.profetas.app.birthday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.account.AccountRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirthdaysState(
    val loading: Boolean = false,
    val failure: String? = null,
    val birthdays: List<Birthday> = emptyList(),
)

class BirthdaysViewModel(
    private val birthdays: BirthdayRepository,
    private val account: AccountRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(BirthdaysState())

    val state: StateFlow<BirthdaysState> = mutableState.asStateFlow()

    // A RLS só devolve linhas para quem está logado: um visitante nem chega a chamar o repositório.
    fun reload() {
        if (state.value.loading) {
            return
        }

        if (account.session == null) {
            mutableState.update { current -> current.copy(failure = null, birthdays = emptyList()) }
            return
        }

        mutableState.update { current -> current.copy(loading = true, failure = null) }

        viewModelScope.launch {
            try {
                val list = birthdays.all()

                mutableState.update { current -> current.copy(loading = false, birthdays = list) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                val message = e.message ?: "Não foi possível carregar os aniversariantes."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
    }

    fun byMonth(month: Int): List<Birthday> = state.value.birthdays
        .filter { birthday -> birthday.month == month }
        .sortedBy { birthday -> birthday.day }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                BirthdaysViewModel(birthdays = container.birthdays, account = container.account)
            }
        }
    }
}
