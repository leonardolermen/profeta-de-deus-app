package br.org.profetas.app.account

import br.org.profetas.app.support.http.*
import br.org.profetas.app.support.session.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*

class ConfirmedLoginTest {
    private val server = MockWebServer()
    private val storage = object : SessionStorage {
        var current: Session? = null
        override fun load() = current
        override fun save(session: Session) { current = session }
        override fun clear() { current = null }
    }
    @Before fun start() { server.start() }
    @After fun stop() { server.shutdown() }

    @Test fun confirmedEmailEntersWithoutManualApproval() = runTest {
        val client = SupabaseClient(SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon", allowLocalHttp = true), storage, OkHttpClient())
        val account = AccountRepository(client, backgroundScope)
        server.enqueue(MockResponse().setBody(response("\"2026-10-29T00:00:00Z\"")))
        account.login("member@local.test", "test-only")
        assertEquals("member", client.session?.userId)
    }

    @Test fun missingConfirmationDoesNotCreateLocalSession() = runTest {
        val client = SupabaseClient(SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon", allowLocalHttp = true), storage, OkHttpClient())
        val account = AccountRepository(client, backgroundScope)
        server.enqueue(MockResponse().setBody(response("null")))
        try { account.login("member@local.test", "test-only"); fail("Unconfirmed email must fail") }
        catch (_: IllegalStateException) {}
        assertNull(client.session)
        assertNull(storage.current)
    }

    @Test fun signupWithoutSessionStaysOutsideMemberArea() = runTest {
        val client = SupabaseClient(SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon", allowLocalHttp = true), storage, OkHttpClient())
        val account = AccountRepository(client, backgroundScope)
        server.enqueue(MockResponse().setBody("""{"id":"member","email":"member@local.test"}"""))
        assertFalse(account.register("Maria", "member@local.test", "test-only"))
        assertNull(client.session)
    }

    private fun response(confirmed: String) = """{"access_token":"A","refresh_token":"R","user":{"id":"member","email":"member@local.test","email_confirmed_at":$confirmed}}"""
}
