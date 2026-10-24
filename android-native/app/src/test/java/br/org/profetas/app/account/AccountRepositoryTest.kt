package br.org.profetas.app.account

import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class InMemorySessionStorage(private var current: Session?) : SessionStorage {

    override fun load(): Session? = current

    override fun save(session: Session) {
        current = session
    }

    override fun clear() {
        current = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepositoryTest {

    private val server = MockWebServer()

    @Before fun startServer() {
        server.start()
    }

    @After fun stopServer() {
        server.shutdown()
    }

    // 127.0.0.1 com allowLocalHttp é o caminho do Supabase local em debug: evita montar https só para o teste.
    private fun client(): SupabaseClient {
        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val storage = InMemorySessionStorage(Session("A", "R", "admin-id", "admin@local.test"))

        return SupabaseClient(config, storage, OkHttpClient())
    }

    @Test fun failedRefreshLogsOutAndClearsProfile() = runTest {
        val client = client()
        val account = AccountRepository(client, backgroundScope)
        server.enqueue(MockResponse().setBody("""[{"id":"admin-id","nome":"Admin","role":"admin"}]"""))

        account.profile()
        runCurrent()

        assertTrue(account.isLoggedIn.value)
        assertEquals("admin", account.currentProfile.value?.role)

        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(400))

        runCatching { client.get("/rest/v1/eventos") }
        runCurrent()

        assertFalse(account.isLoggedIn.value)
        assertNull(account.currentProfile.value)
    }
}
