package br.org.profetas.app.event

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.event.registration.RegistrationRepository
import br.org.profetas.app.event.reminder.EventReminders
import br.org.profetas.app.event.weekly.WeeklyServiceRepository
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.message.UserMessages
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    // Um membro logado: inscrever exige sessão, e a leitura de eventos não muda com ela.
    private class MemberSessionStorage : SessionStorage {

        override fun load(): Session? = Session("A", "R", "membro-id", "membro@local.test")

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private val server = MockWebServer()

    private val eventResponses = ConcurrentLinkedQueue<MockResponse>()

    @Volatile private var weeklyBody = "[]"

    @Volatile private var enrollResponse = MockResponse().setResponseCode(201)

    private val registrationResponses = ConcurrentLinkedQueue<MockResponse>()

    private val registrationLoads = AtomicInteger()

    private val userMessages = UserMessages()

    private class FakeReminders : EventReminders {

        val enabled = mutableSetOf<String>()

        override fun schedule(event: Event) {
            enabled.add(event.id)
        }

        override fun cancel(eventId: String) {
            enabled.remove(eventId)
        }

        override fun enabledIds(): Set<String> = enabled.toSet()
    }

    // Uma recarga busca eventos e cultos semanais ao mesmo tempo: a resposta vai pela tabela, não pela ordem de
    // chegada.
    @Before fun startServer() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()

                return when {
                    path.startsWith("/rest/v1/eventos") -> eventResponses.poll() ?: MockResponse().setResponseCode(500)
                    path.startsWith("/rest/v1/inscricoes") -> registrations(request)
                    else -> MockResponse().setBody(weeklyBody)
                }
            }
        }
        server.start()
    }

    private fun registrations(request: RecordedRequest): MockResponse {
        if (request.method == "POST") {
            return enrollResponse
        }

        registrationLoads.incrementAndGet()

        return registrationResponses.poll() ?: MockResponse().setBody("[]")
    }

    @After fun stopServer() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    // O "repositório falso" é o EventRepository de verdade contra um MockWebServer: o ViewModel vê exatamente
    // o que o app veria do Supabase, sem abrir a classe do repositório só para o teste.
    private fun TestScope.viewModel(scope: CoroutineScope): EventsViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, MemberSessionStorage(), OkHttpClient())
        val guard = ContentWriteGuard(AccountRepository(client, scope))

        return EventsViewModel(
            events = EventRepository(client, guard),
            weekly = WeeklyServiceRepository(client, guard),
            registrations = RegistrationRepository(client, guard),
            reminders = FakeReminders(),
            userMessages = userMessages,
        )
    }

    private fun eventRow(
        id: String,
        date: String,
        title: String = "Evento $id",
        description: String = "",
        type: String = "culto",
    ): String =
        """{"id":"$id","titulo":"$title","descricao":"$description","data_evento":"$date",""" +
            """"tipo":"$type","imagem":null}"""

    private suspend fun TestScope.loaded(vararg rows: String): EventsViewModel {
        eventResponses.add(MockResponse().setBody(rows.joinToString(",", "[", "]")))

        val viewModel = viewModel(backgroundScope)
        viewModel.state.first { state -> !state.loading }

        return viewModel
    }

    @Test fun upcomingSkipsPastAndSortsByDate() = runTest {
        val rows = listOf(
            eventRow("passado", "2000-01-01T10:00:00-03:00"),
            eventRow("depois", "2100-06-01T10:00:00-03:00"),
            eventRow("antes", "2100-01-01T10:00:00-03:00"),
        )
        eventResponses.add(MockResponse().setBody(rows.joinToString(",", "[", "]")))

        val viewModel = viewModel(backgroundScope)
        viewModel.state.first { state -> !state.loading }

        assertEquals(listOf("antes", "depois"), viewModel.upcoming(5).map { event -> event.id })
    }

    @Test fun failureKeepsPreviousEvents() = runTest {
        eventResponses.add(MockResponse().setBody("[" + eventRow("um", "2100-01-01T10:00:00-03:00") + "]"))
        eventResponses.add(MockResponse().setResponseCode(500).setBody("""{"message":"banco fora do ar"}"""))

        val viewModel = viewModel(backgroundScope)
        viewModel.state.first { state -> !state.loading }

        viewModel.reload()
        val state = viewModel.state.first { current -> !current.loading }

        assertEquals("banco fora do ar", state.failure)
        assertEquals(listOf("um"), state.events.map { event -> event.id })
    }

    @Test fun filterMatchesTitleOrDescriptionIgnoringCase() = runTest {
        val viewModel = loaded(
            eventRow("titulo", "2100-01-01T10:00:00-03:00", title = "Retiro de Jovens"),
            eventRow("descricao", "2100-02-01T10:00:00-03:00", description = "Um retiro para a família"),
            eventRow("outro", "2100-03-01T10:00:00-03:00", title = "Culto de Santa Ceia"),
        )

        val found = viewModel.filtered(query = "RETIRO", type = null, includePast = false)

        assertEquals(listOf("titulo", "descricao"), found.map { event -> event.id })
    }

    @Test fun filterByType() = runTest {
        val viewModel = loaded(
            eventRow("culto", "2100-01-01T10:00:00-03:00", type = "culto"),
            eventRow("jovens", "2100-02-01T10:00:00-03:00", type = "jovens"),
        )

        val youth = viewModel.filtered(query = "", type = EventType.JOVENS, includePast = false)
        val everything = viewModel.filtered(query = "", type = null, includePast = false)

        assertEquals(listOf("jovens"), youth.map { event -> event.id })
        assertEquals(listOf("culto", "jovens"), everything.map { event -> event.id })
    }

    @Test fun pastHiddenUnlessIncluded() = runTest {
        val viewModel = loaded(
            eventRow("passado", "2026-01-01T10:00:00-03:00"),
            eventRow("futuro", "2026-12-01T10:00:00-03:00"),
        )
        val now = Instant.parse("2026-10-23T12:00:00Z")

        val upcomingOnly = viewModel.filtered(query = "", type = null, includePast = false, now = now)
        val withPast = viewModel.filtered(query = "", type = null, includePast = true, now = now)

        assertEquals(listOf("futuro"), upcomingOnly.map { event -> event.id })
        assertEquals(listOf("passado", "futuro"), withPast.map { event -> event.id })
    }

    @Test fun weeklyServicesLoadWithEvents() = runTest {
        weeklyBody = """[{"id":"w","nome":"Culto de Libertação","dia_semana":3,"horarios":["19:30"],""" +
            """"destaque":"","local":"","ordem":0}]"""

        val viewModel = loaded(eventRow("um", "2100-01-01T10:00:00-03:00"))

        val names = viewModel.state.value.weeklyServices.map { service -> service.name }
        assertEquals(listOf("Culto de Libertação"), names)
    }

    @Test fun nextBaptismIsEarliestFutureBaptism() = runTest {
        val viewModel = loaded(
            eventRow("passado", "2026-01-01T10:00:00-03:00", type = "batismo"),
            eventRow("depois", "2027-03-01T10:00:00-03:00", type = "batismo"),
            eventRow("culto", "2026-11-01T10:00:00-03:00", type = "culto"),
            eventRow("proximo", "2026-12-01T10:00:00-03:00", type = "batismo"),
        )
        val now = Instant.parse("2026-10-23T12:00:00Z")

        assertEquals("proximo", viewModel.nextBaptism(now)?.id)
    }

    @Test fun campaignsOnlyFutureCampaigns() = runTest {
        val viewModel = loaded(
            eventRow("passada", "2026-01-01T10:00:00-03:00", type = "campanha"),
            eventRow("culto", "2026-11-01T10:00:00-03:00", type = "culto"),
            eventRow("primeira", "2026-11-22T10:00:00-03:00", type = "campanha"),
            eventRow("segunda", "2027-02-01T10:00:00-03:00", type = "campanha"),
        )
        val now = Instant.parse("2026-10-23T12:00:00Z")

        assertEquals(listOf("primeira", "segunda"), viewModel.campaigns(now).map { event -> event.id })
    }

    @Test fun enrollShowsSuccessAndReloadsRegistrations() = runTest {
        val viewModel = loaded(eventRow("retiro", "2100-01-01T10:00:00-03:00"))
        registrationResponses.add(
            MockResponse().setBody(
                """[{"id":"r1","evento_id":"retiro","nome":"Maria","telefone":"11999998888",""" +
                    """"usuario_id":"membro-id"}]""",
            ),
        )

        val message = async(start = CoroutineStart.UNDISPATCHED) { userMessages.messages.first() }
        viewModel.enroll("retiro", "Maria", "11999998888")

        assertEquals("Inscrição confirmada!", message.await())
        val state = viewModel.state.first { current -> current.registrations.isNotEmpty() }
        assertEquals(listOf("r1"), state.registrations.map { registration -> registration.id })
        assertEquals(1, registrationLoads.get())
    }

    @Test fun enrollFailureShowsRepositoryMessage() = runTest {
        val viewModel = loaded(eventRow("retiro", "2100-01-01T10:00:00-03:00"))
        enrollResponse = MockResponse().setResponseCode(409).setBody("""{"message":"duplicate key"}""")

        val message = async(start = CoroutineStart.UNDISPATCHED) { userMessages.messages.first() }
        viewModel.enroll("retiro", "Maria", "11999998888")

        assertEquals("Este cadastro já existe.", message.await())
    }
}
