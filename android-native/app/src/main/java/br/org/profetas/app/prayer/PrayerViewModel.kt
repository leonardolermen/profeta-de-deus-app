package br.org.profetas.app.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.account.Profile
import br.org.profetas.app.account.ContentAccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import br.org.profetas.app.support.message.UserMessages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Visíveis também para PrayerScreen: a tela usa os mesmos limites para o contador e para desabilitar o envio.
const val MIN_NAME_LENGTH = 2
const val MAX_NAME_LENGTH = 120
const val MIN_TEXT_LENGTH = 5
const val MAX_TEXT_LENGTH = 4000

data class PrayerState(
    val sending: Boolean = false,
    val canManage: Boolean = false,
    val received: List<Prayer> = emptyList(),
    val loading: Boolean = false,
    val failure: String? = null,
)

class PrayerViewModel(
    private val prayers: PrayerRepository,
    private val messages: UserMessages,
    profiles: Flow<Profile?> = flowOf(null),
) : ViewModel() {

    private val mutableState = MutableStateFlow(PrayerState())

    val state: StateFlow<PrayerState> = mutableState.asStateFlow()
    private var receivedJob: Job? = null

    init {
        viewModelScope.launch {
            profiles.collect { profile ->
                receivedJob?.cancel()
                mutableState.update { it.copy(canManage = ContentAccess.canManage(profile), received = emptyList(), loading = false, failure = null) }
            }
        }
    }

    fun loadReceived() {
        if (!state.value.canManage || state.value.loading) return
        mutableState.update { it.copy(loading = true, failure = null) }
        receivedJob = viewModelScope.launch {
            try {
                val received = prayers.all()
                mutableState.update { it.copy(received = received, loading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.update { it.copy(received = emptyList(), loading = false, failure = e.message ?: "Não foi possível carregar os pedidos.") }
            }
        }
    }

    // Evento de disparo único: o PrayerViewModel é criado uma vez em ProfetasApp e sobrevive à navegação, então um
    // campo "sent" no state ficaria preso em true e mandaria de volta sozinho em toda visita seguinte à tela. Um
    // SharedFlow sem replay só alcança quem já está coletando no momento do envio (a tela aberta agora).
    private val sentEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val sent: SharedFlow<Unit> = sentEvents.asSharedFlow()

    // Mesmas regras da tela Kotlin anterior: sem equivalente no Expo, que só checava campo vazio.
    fun send(name: String, text: String) {
        if (state.value.sending) {
            return
        }

        val trimmedName = name.trim()
        val trimmedText = text.trim()

        if (trimmedName.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            return
        }

        if (trimmedText.length !in MIN_TEXT_LENGTH..MAX_TEXT_LENGTH) {
            return
        }

        mutableState.update { current -> current.copy(sending = true) }

        viewModelScope.launch {
            try {
                prayers.send(trimmedName, trimmedText)

                messages.show("Pedido enviado à equipe pastoral.")
                sentEvents.tryEmit(Unit)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                messages.show(e.message ?: "Não foi possível enviar seu pedido. Tente novamente.")
            } finally {
                mutableState.update { current -> current.copy(sending = false) }
            }
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                PrayerViewModel(prayers = container.prayers, messages = container.userMessages, profiles = container.account.currentProfile)
            }
        }
    }
}
