package br.org.profetas.app.account

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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private class MemberSessionStorage : SessionStorage {

        private var current: Session? = Session("A", "R", "member-id", "membro@local.test")

        override fun load(): Session? = current

        override fun save(session: Session) {
            current = session
        }

        override fun clear() {
            current = null
        }
    }

    private val server = MockWebServer()

    private val userMessages = UserMessages()

    private val requestedPaths = mutableListOf<String>()

    @Before fun startServer() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                requestedPaths.add(request.path.orEmpty())

                return when {
                    request.path.orEmpty().startsWith("/auth/v1/logout") -> MockResponse().setResponseCode(500)
                    request.path.orEmpty().startsWith("/rest/v1/perfis") ->
                        MockResponse().setBody("""[{"id":"member-id","nome":"Maria","role":"member"}]""")
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
    }

    @After fun stopServer() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    private fun TestScope.account(scope: CoroutineScope): AccountRepository {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)

        return AccountRepository(SupabaseClient(config, MemberSessionStorage(), OkHttpClient()), scope)
    }

    // O servidor recusa o logout (500), mas o aparelho tem que sair mesmo assim: manter o perfil de uma sessão
    // que o usuário mandou encerrar deixaria dados de membro visíveis numa conta que ele acha que fechou.
    @Test fun logoutClearsProfileEvenWhenServerFails() = runTest {
        val account = account(backgroundScope)
        val viewModel = AccountViewModel(account, userMessages)

        account.profile()
        runCurrent()
        assertNotNull(account.currentProfile.value)

        val message = async(start = CoroutineStart.UNDISPATCHED) { userMessages.messages.first() }
        viewModel.logout()

        // A mensagem só sai depois que o logout terminou (a rede roda fora do dispatcher de teste).
        assertEquals("Você saiu da conta.", message.await())
        runCurrent()

        assertNull(account.currentProfile.value)
        assertEquals(false, account.isLoggedIn.value)
        assertEquals(true, requestedPaths.any { path -> path.startsWith("/auth/v1/logout") })
    }

    @Test fun invalidEmailIsReportedWithoutCallingServer() = runTest {
        val viewModel = AccountViewModel(account(backgroundScope), userMessages)

        val message = async(start = CoroutineStart.UNDISPATCHED) { userMessages.messages.first() }
        viewModel.login("a@", "senha-local-123")
        runCurrent()

        assertEquals("Informe um e-mail válido.", message.await())
        assertEquals(emptyList<String>(), requestedPaths)
    }
}
