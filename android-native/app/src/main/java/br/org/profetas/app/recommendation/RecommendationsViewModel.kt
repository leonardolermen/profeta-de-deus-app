package br.org.profetas.app.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.support.message.UserMessages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendationsState(
    val items: List<Recommendation> = emptyList(), val loading: Boolean = false,
    val loaded: Boolean = false, val saving: Boolean = false, val failure: String? = null,
    val canManage: Boolean = false,
)

class RecommendationsViewModel(
    private val repository: RecommendationRepository,
    private val account: AccountRepository,
    private val messages: UserMessages,
) : ViewModel() {
    private val mutable = MutableStateFlow(RecommendationsState())
    val state = mutable.asStateFlow()
    private val savedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val saved = savedEvents.asSharedFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            account.currentProfile.collect { profile ->
                mutable.update { it.copy(canManage = ContentAccess.canManage(profile)) }
            }
        }
    }

    fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutable.update { it.copy(loading = true, failure = null) }
            try {
                val items = repository.all()
                mutable.update { it.copy(items = items, loading = false, loaded = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mutable.update { it.copy(loading = false, failure = "Não foi possível carregar as indicações. Verifique sua conexão e tente novamente.") }
            }
        }
    }

    fun save(id: String?, draft: RecommendationDraft) = change(true) { repository.save(id, draft) }
    fun delete(id: String) = change(false) { repository.delete(id) }

    private fun change(closeEditor: Boolean, action: suspend () -> Unit) {
        if (state.value.saving) return
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                action()
                reload()
                messages.show(if (closeEditor) "Indicação salva." else "Indicação excluída.")
                if (closeEditor) savedEvents.tryEmit(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                messages.show(e.message ?: "Não foi possível salvar a alteração.")
            } finally {
                mutable.update { it.copy(saving = false) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as ProfetasApplication).container
                RecommendationsViewModel(container.recommendations, container.account, container.userMessages)
            }
        }
    }
}
