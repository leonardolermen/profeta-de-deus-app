package br.org.profetas.app.prayer

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
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
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerViewModelTest {

    // A versão 2.4.0 exige uma conta autenticada para enviar pedidos.
    private class MemberSessionStorage : SessionStorage {

        override fun load(): Session? = Session("A", "R", "member", "member@local.test")

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private val server = MockWebServer()

    private val prayerPosts = AtomicInteger()

    private val userMessages = UserMessages()

    @Before fun startServer() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                prayerPosts.incrementAndGet()

                return MockResponse().setResponseCode(201)
            }
        }
        server.start()
    }

    @After fun stopServer() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    // O "repositório falso" é o PrayerRepository de verdade contra um MockWebServer, como nos outros ViewModels.
    private fun TestScope.viewModel(scope: CoroutineScope): PrayerViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, MemberSessionStorage(), OkHttpClient())
        val guard = ContentWriteGuard(AccountRepository(client, scope))

        return PrayerViewModel(prayers = PrayerRepository(client, guard), messages = userMessages)
    }

    @Test fun rejectsShortRequestWithoutCallingRepository() = runTest {
        val viewModel = viewModel(backgroundScope)

        viewModel.send("Maria Aparecida", "oi")

        assertEquals(0, prayerPosts.get())
    }

    @Test fun successMessageIsTheExistingOne() = runTest {
        val viewModel = viewModel(backgroundScope)

        val message = async(start = CoroutineStart.UNDISPATCHED) { userMessages.messages.first() }
        viewModel.send("Maria Aparecida", "Por favor, orem pela minha família nesse momento difícil.")

        assertEquals("Pedido enviado à equipe pastoral.", message.await())
        assertEquals(1, prayerPosts.get())
    }

    // O PrayerViewModel é criado uma vez em ProfetasApp e sobrevive à navegação: um envio bem-sucedido não pode
    // deixar a tela presa, nem impedir um segundo pedido na mesma visita ou numa visita seguinte.
    @Test fun canSendAgainAfterSuccess() = runTest {
        val viewModel = viewModel(backgroundScope)

        val firstSent = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sent.first() }
        viewModel.send("Maria Aparecida", "Por favor, orem pela minha família nesse momento difícil.")
        firstSent.await()

        assertEquals(false, viewModel.state.value.sending)

        val secondSent = async(start = CoroutineStart.UNDISPATCHED) { viewModel.sent.first() }
        viewModel.send("Carlos Eduardo", "Oremos também pela saúde do meu pai, que está internado agora.")
        secondSent.await()

        assertEquals(false, viewModel.state.value.sending)
        assertEquals(2, prayerPosts.get())
    }
}
