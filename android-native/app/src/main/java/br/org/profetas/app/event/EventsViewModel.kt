package br.org.profetas.app.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.event.weekly.WeeklyService
import br.org.profetas.app.event.weekly.WeeklyServiceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class EventsState(
    val loading: Boolean = false,
    val failure: String? = null,
    val events: List<Event> = emptyList(),
    val weeklyServices: List<WeeklyService> = emptyList(),
)

class EventsViewModel(
    private val events: EventRepository,
    private val weekly: WeeklyServiceRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(EventsState())

    val state: StateFlow<EventsState> = mutableState.asStateFlow()

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

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                EventsViewModel(application.container.events, application.container.weekly)
            }
        }
    }
}
