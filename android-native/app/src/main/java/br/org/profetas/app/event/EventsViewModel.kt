package br.org.profetas.app.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.event.registration.Registration
import br.org.profetas.app.event.registration.RegistrationRepository
import br.org.profetas.app.event.reminder.EventReminders
import br.org.profetas.app.event.weekly.WeeklyService
import br.org.profetas.app.event.weekly.WeeklyServiceRepository
import br.org.profetas.app.support.message.UserMessages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class EventsState(
    val loading: Boolean = false,
    val failure: String? = null,
    val events: List<Event> = emptyList(),
    val weeklyServices: List<WeeklyService> = emptyList(),
    val registrations: List<Registration> = emptyList(),
    val reminderIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
)

class EventsViewModel(
    private val events: EventRepository,
    private val weekly: WeeklyServiceRepository,
    private val registrations: RegistrationRepository,
    private val reminders: EventReminders,
    private val userMessages: UserMessages,
    private val accountId: StateFlow<String?>,
) : ViewModel() {

    private val mutableState = MutableStateFlow(EventsState(reminderIds = reminders.enabledIds()))

    val state: StateFlow<EventsState> = mutableState.asStateFlow()

    init {
        reload()

        // Inscrições têm nome e telefone de quem se inscreveu: a lista de uma conta não pode sobreviver à troca para
        // outra, nem quando a recarga da nova conta falha (sem rede). O valor inicial é pulado porque a lista ainda
        // está vazia e as telas de inscrição pedem a carga ao abrir.
        viewModelScope.launch {
            accountId.drop(1).collectLatest { id ->
                mutableState.update { current -> current.copy(registrations = emptyList()) }

                if (id != null) {
                    refreshRegistrations()
                }
            }
        }
    }

    fun reload() {
        if (state.value.loading) {
            return
        }

        mutableState.update { current -> current.copy(loading = true, failure = null) }

        viewModelScope.launch {
            try {
                coroutineScope {
                    val loadedEvents = async { events.all() }
                    val loadedWeekly = async { weekly.all() }

                    val eventList = loadedEvents.await()
                    val weeklyList = loadedWeekly.await()

                    mutableState.update { current ->
                        current.copy(loading = false, events = eventList, weeklyServices = weeklyList)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                // Mantém a lista anterior: numa falha de rede o membro continua vendo a agenda que já tinha.
                val message = e.message ?: "Não foi possível carregar os eventos."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
    }

    fun upcoming(limit: Int, now: Instant = Instant.now()): List<Event> = state.value.events
        .mapNotNull { event -> eventInstant(event.date)?.let { instant -> event to instant } }
        .filter { (_, instant) -> instant.isAfter(now) }
        .sortedBy { (_, instant) -> instant }
        .take(limit)
        .map { (event, _) -> event }

    // Mantém a ordem do banco (data crescente). `includePast` só escolhe quais linhas entram, como os outros filtros.
    fun filtered(query: String, type: EventType?, includePast: Boolean, now: Instant = Instant.now()): List<Event> =
        state.value.events.filter { event ->
            val instant = eventInstant(event.date)

            val isInPeriod = instant != null && (includePast || instant.isAfter(now))
            val isOfType = type == null || event.type == type
            val matchesQuery = event.title.contains(query, ignoreCase = true) ||
                event.description.contains(query, ignoreCase = true)

            isInPeriod && isOfType && matchesQuery
        }

    fun nextBaptism(now: Instant = Instant.now()): Event? = futureOfType(EventType.BATISMO, now).firstOrNull()

    fun campaigns(now: Instant = Instant.now()): List<Event> = futureOfType(EventType.CAMPANHA, now)

    private fun futureOfType(type: EventType, now: Instant): List<Event> = state.value.events
        .filter { event -> event.type == type && isFuture(event.date, now) }
        .sortedBy { event -> eventInstant(event.date) }

    // Sem sessão o repositório devolve lista vazia: é assim que as inscrições somem depois de sair da conta.
    fun reloadRegistrations() {
        viewModelScope.launch {
            refreshRegistrations()
        }
    }

    fun enroll(eventId: String, name: String, phone: String) = save("Inscrição confirmada!") {
        registrations.enroll(eventId, name, phone)
    }

    fun cancelRegistration(id: String) = save("Inscrição cancelada.") {
        registrations.cancel(id)
    }

    fun refreshReminders() {
        mutableState.update { current -> current.copy(reminderIds = reminders.enabledIds()) }
    }

    fun setReminder(event: Event) {
        reminders.schedule(event)

        refreshReminders()
    }

    fun removeReminder(eventId: String) {
        reminders.cancel(eventId)

        refreshReminders()
    }

    private fun save(success: String, block: suspend () -> Unit) {
        if (state.value.isSaving) {
            return
        }

        mutableState.update { current -> current.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                block()

                userMessages.show(success)
                refreshRegistrations()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                userMessages.show(e.message ?: "Não foi possível concluir. Verifique sua conexão.")
            } finally {
                mutableState.update { current -> current.copy(isSaving = false) }
            }
        }
    }

    // Falhar ao recarregar a lista não desfaz o que já foi salvo: fica a lista anterior até a próxima tentativa.
    // A conta pode mudar enquanto a lista vem do servidor; a resposta atrasada é de outra pessoa e é descartada.
    private suspend fun refreshRegistrations() {
        val requestedFor = accountId.value

        try {
            val mine = registrations.mine()

            if (accountId.value == requestedFor) {
                mutableState.update { current -> current.copy(registrations = mine) }
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                EventsViewModel(
                    events = container.events,
                    weekly = container.weekly,
                    registrations = container.registrations,
                    reminders = container.reminders,
                    userMessages = container.userMessages,
                    accountId = container.account.accountId,
                )
            }
        }
    }
}
