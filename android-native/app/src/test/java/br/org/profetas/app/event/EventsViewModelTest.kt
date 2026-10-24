package br.org.profetas.app.event

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private class SignedOutStorage : SessionStorage {

        override fun load(): Session? = null

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private val server = MockWebServer()

    @Before fun startServer() {
        server.start()
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
        val client = SupabaseClient(config, SignedOutStorage(), OkHttpClient())
        val guard = ContentWriteGuard(AccountRepository(client, scope))

        return EventsViewModel(EventRepository(client, guard))
    }

    private fun eventRow(id: String, date: String): String =
        """{"id":"$id","titulo":"Evento $id","descricao":"","data_evento":"$date","tipo":"culto","imagem":null}"""

    @Test fun upcomingSkipsPastAndSortsByDate() = runTest {
        val rows = listOf(
            eventRow("passado", "2000-01-01T10:00:00-03:00"),
            eventRow("depois", "2100-06-01T10:00:00-03:00"),
            eventRow("antes", "2100-01-01T10:00:00-03:00"),
        )
        server.enqueue(MockResponse().setBody(rows.joinToString(",", "[", "]")))

        val viewModel = viewModel(backgroundScope)
        viewModel.state.first { state -> !state.loading }

        assertEquals(listOf("antes", "depois"), viewModel.upcoming(5).map { event -> event.id })
    }

    @Test fun failureKeepsPreviousEvents() = runTest {
        server.enqueue(MockResponse().setBody("[" + eventRow("um", "2100-01-01T10:00:00-03:00") + "]"))
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"message":"banco fora do ar"}"""))

        val viewModel = viewModel(backgroundScope)
        viewModel.state.first { state -> !state.loading }

        viewModel.reload()
        val state = viewModel.state.first { current -> !current.loading }

        assertEquals("banco fora do ar", state.failure)
        assertEquals(listOf("um"), state.events.map { event -> event.id })
    }
}
