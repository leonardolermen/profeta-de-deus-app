package br.org.profetas.app.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
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

data class AccountState(val saving: Boolean = false)

enum class AccountEvent {
    SIGNED_IN,
    RECOVERY_ACCEPTED,
    PASSWORD_UPDATED,
}

class AccountViewModel(private val account: AccountRepository, private val messages: UserMessages) : ViewModel() {

    private val mutableState = MutableStateFlow(AccountState())

    val state: StateFlow<AccountState> = mutableState.asStateFlow()

    // Eventos de disparo único, não campos do state: o AccountViewModel é criado uma vez em ProfetasApp e sobrevive
    // à navegação, então um "signedIn = true" ficaria preso e fecharia a tela de login em toda visita seguinte (o
    // mesmo defeito que o PrayerViewModel teve). Um SharedFlow sem replay só alcança quem coleta no momento.
    private val mutableEvents = MutableSharedFlow<AccountEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<AccountEvent> = mutableEvents.asSharedFlow()

    val isLoggedIn: StateFlow<Boolean> get() = account.isLoggedIn

    val profile: StateFlow<Profile?> get() = account.currentProfile

    private val mutableProfileFailure = MutableStateFlow<String?>(null)

    // A administração depende do perfil: a falha precisa durar como estado (para oferecer "Tentar novamente"), não
    // só passar pelo snackbar.
    val profileFailure: StateFlow<String?> = mutableProfileFailure.asStateFlow()

    val email: String get() = account.session?.email.orEmpty()

    fun login(email: String, password: String) {
        perform("Bem-vindo!", AccountEvent.SIGNED_IN) {
            val validEmail = Email.of(email)
            require(password.isNotEmpty()) { "Informe sua senha." }

            account.login(validEmail.value, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        val success = "Cadastro recebido. Se a confirmação estiver habilitada, confira seu e-mail."

        perform(success, AccountEvent.SIGNED_IN) {
            val validName = PersonName.of(name)
            val validEmail = Email.of(email)
            val validPassword = NewPassword.of(password)

            account.register(validName.value, validEmail.value, validPassword.value)
        }
    }

    fun requestRecovery(email: String) {
        perform("Se houver uma conta com esse e-mail, você receberá as instruções de recuperação.") {
            account.requestRecovery(Email.of(email).value)
        }
    }

    fun acceptRecovery(link: String) {
        perform("Defina sua nova senha no perfil.", AccountEvent.RECOVERY_ACCEPTED) {
            account.acceptRecovery(link)
        }
    }

    fun saveName(name: String) {
        perform("Perfil atualizado.") {
            account.saveName(PersonName.of(name).value)
            account.profile()
        }
    }

    fun updatePassword(password: String, confirmation: String) {
        perform("Senha atualizada.", AccountEvent.PASSWORD_UPDATED) {
            val validPassword = NewPassword.of(password)
            require(password == confirmation) { "As senhas não conferem." }

            account.updatePassword(validPassword.value)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                account.logout()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                // O repositório já largou a sessão local no finally: a falha foi só avisar o servidor, e o aparelho
                // saiu da conta de qualquer jeito. Mostrar o erro faria quem saiu achar que ainda está dentro.
            }

            messages.show("Você saiu da conta.")
        }
    }

    // O perfil só existe depois de account.profile(): quem abre a tela do perfil pede que seja carregado.
    fun loadProfile() {
        mutableProfileFailure.value = null

        viewModelScope.launch {
            try {
                account.profile()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                val message = e.message ?: FAILURE_MESSAGE
                mutableProfileFailure.value = message
                messages.show(message)
            }
        }
    }

    private fun perform(success: String, event: AccountEvent? = null, block: suspend () -> Unit) {
        if (state.value.saving) {
            return
        }

        mutableState.update { current -> current.copy(saving = true) }

        viewModelScope.launch {
            try {
                block()

                messages.show(success)
                event?.let(mutableEvents::tryEmit)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                messages.show(e.message ?: FAILURE_MESSAGE)
            } finally {
                mutableState.update { current -> current.copy(saving = false) }
            }
        }
    }

    companion object {

        private const val FAILURE_MESSAGE = "Não foi possível concluir. Verifique sua conexão."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                AccountViewModel(account = container.account, messages = container.userMessages)
            }
        }
    }
}
