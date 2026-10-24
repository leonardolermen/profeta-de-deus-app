package br.org.profetas.app.bible

import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import br.org.profetas.app.support.storage.InMemoryPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class BibleViewModelTest {

    private object NoSessionStorage : SessionStorage {

        override fun load(): Session? = null

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private val server = MockWebServer()

    private val releaseFirstChapter = CountDownLatch(1)

    private val firstChapterDelivered = CountDownLatch(1)

    private val psalms = Book(name = "Salmos", abbreviation = "sl", chapters = 150)

    // O capítulo 1 só responde quando o teste solta: é a resposta atrasada que chega depois do capítulo 2.
    @Before fun startServer() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()

                if (path.endsWith("/sl/1")) {
                    releaseFirstChapter.await(10, TimeUnit.SECONDS)
                }

                val number = path.substringAfterLast('/')
                return MockResponse().setBody("""{"verses":[{"number":1,"text":"Capítulo $number"}]}""")
            }
        }
        server.start()
    }

    @After fun stopServer() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    // A API da Bíblia tem endereço fixo no repositório; o interceptor manda as chamadas para o MockWebServer.
    private fun bibleRepository(): BibleRepository {
        val toServer = Interceptor { chain ->
            val original = chain.request().url
            val redirected = original.newBuilder()
                .scheme("http")
                .host(server.hostName)
                .port(server.port)
                .build()

            val response = chain.proceed(chain.request().newBuilder().url(redirected).build())

            if (original.encodedPath.endsWith("/sl/1")) {
                firstChapterDelivered.countDown()
            }

            response
        }

        val http = OkHttpClient.Builder().addInterceptor(toServer).build()
        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)

        return BibleRepository(SupabaseClient(config, NoSessionStorage, http), token = "")
    }

    @Test fun staleChapterIsDiscarded() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val viewModel = BibleViewModel(bibleRepository(), FavoriteVerses(InMemoryPreferences()) { "visitante" })

        viewModel.openChapter(psalms, 1)
        viewModel.openChapter(psalms, 2)
        viewModel.state.first { state -> state.chapter?.number == 2 }

        releaseFirstChapter.countDown()
        firstChapterDelivered.await(10, TimeUnit.SECONDS)

        // A resposta velha volta numa thread de IO e, com o Main sem fila deste teste, é tratada ali mesmo logo
        // depois de entregue; não há sinal depois disso para esperar, então o teste dá esse instante a ela.
        Thread.sleep(300)

        val state = viewModel.state.value
        assertEquals(2, state.chapter?.number)
        assertEquals("Capítulo 2", state.chapter?.verses?.single()?.text)
        assertFalse(state.isLoadingChapter)
    }
}
