package br.org.profetas.app.prayer

import br.org.profetas.app.account.*
import br.org.profetas.app.support.http.*
import br.org.profetas.app.support.session.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*

class PrayerAuthenticationTest {
    private val server = MockWebServer()
    @Before fun start() { server.start() }
    @After fun stop() { server.shutdown() }
    @Test fun loggedOutRequestNeverReachesServer() = runTest {
        val storage = object : SessionStorage {
            override fun load(): Session? = null
            override fun save(session: Session) {}
            override fun clear() {}
        }
        val client = SupabaseClient(SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon", allowLocalHttp = true), storage, OkHttpClient())
        val repo = PrayerRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope)))
        try { repo.send("Maria", "Orem pela família"); fail("Login required") }
        catch (_: IllegalStateException) {}
        assertEquals(0, server.requestCount)
    }

    @Test fun personalRequestIsBoundToLoggedInAccount() = runTest {
        val storage = object : SessionStorage {
            override fun load() = Session("A", "R", "member", "member@local.test")
            override fun save(session: Session) {}
            override fun clear() {}
        }
        val client = SupabaseClient(SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon", allowLocalHttp = true), storage, OkHttpClient())
        server.enqueue(MockResponse().setResponseCode(201))
        PrayerRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope))).send("Maria", "Orem pela família")
        val request = server.takeRequest()
        assertEquals("member", JSONObject(request.body.readUtf8()).getString("usuario_id"))
        assertEquals("Bearer A", request.getHeader("Authorization"))
    }
}
