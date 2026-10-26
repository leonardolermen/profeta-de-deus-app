package br.org.profetas.app.event.weekly

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeeklyServiceRepositoryTest {

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
        server.shutdown()
    }

    @Test fun ordersByOrderThenDay() = runTest {
        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, SignedOutStorage(), OkHttpClient())
        val repository = WeeklyServiceRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope)))
        server.enqueue(MockResponse().setBody("[]"))

        repository.all()

        assertEquals("/rest/v1/cultos_semanais?select=*&order=ordem.asc,dia_semana.asc", server.takeRequest().path)
    }
}
