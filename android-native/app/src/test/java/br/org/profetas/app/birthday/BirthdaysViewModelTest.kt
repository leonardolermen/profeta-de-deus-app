package br.org.profetas.app.birthday

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
class BirthdaysViewModelTest {

    // Um membro logado: a RLS de aniversariantes só devolve linhas para quem está logado.
    private class MemberSessionStorage : SessionStorage {

        override fun load(): Session? = Session("A", "R", "membro-id", "membro@local.test")

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private class VisitorSessionStorage : SessionStorage {

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

    // O "repositório falso" é o BirthdayRepository de verdade contra um MockWebServer, como nos outros ViewModels.
    private fun TestScope.viewModel(scope: CoroutineScope, storage: SessionStorage): BirthdaysViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, storage, OkHttpClient())
        val account = AccountRepository(client, scope)
        val guard = ContentWriteGuard(account)

        return BirthdaysViewModel(birthdays = BirthdayRepository(client, guard), account = account)
    }

    private fun birthdayRow(id: String, name: String, day: Int, month: Int): String =
        """{"id":"$id","nome":"$name","dia":$day,"mes":$month,"consentimento":true}"""

    @Test fun byMonthSortedByDay() = runTest {
        val rows = listOf(
            birthdayRow("tarde", "Carlos", day = 20, month = 11),
            birthdayRow("cedo", "Ana", day = 5, month = 11),
            birthdayRow("outro-mes", "Pedro", day = 1, month = 10),
        )
        server.enqueue(MockResponse().setBody(rows.joinToString(",", "[", "]")))

        val viewModel = viewModel(backgroundScope, MemberSessionStorage())
        viewModel.reload()
        viewModel.state.first { state -> !state.loading }

        val november = viewModel.byMonth(11)

        assertEquals(listOf("Ana", "Carlos"), november.map { birthday -> birthday.name })
    }

    @Test fun visitorHasNoBirthdaysWithoutCallingRepository() = runTest {
        val viewModel = viewModel(backgroundScope, VisitorSessionStorage())

        viewModel.reload()

        assertEquals(0, server.requestCount)
        assertEquals(emptyList<Birthday>(), viewModel.byMonth(11))
    }
}
