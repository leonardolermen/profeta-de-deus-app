package br.org.profetas.app.recommendation

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.*
import br.org.profetas.app.support.session.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*

class RecommendationRepositoryTest {
    private val server = MockWebServer()
    @Before fun start() { server.start() }
    @After fun stop() { server.shutdown() }

    @Test fun memberReadsRecommendations() = runTest {
        val client = client()
        val repository = RecommendationRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope)))
        server.enqueue(MockResponse().setBody("""[{"id":"1","titulo":"Doces da Maria","descricao":"Encomendas para festas","contato":"Contato autorizado","link":"https://example.org"}]"""))
        val item = repository.all().single()
        assertEquals("Doces da Maria", item.title)
        assertEquals("Contato autorizado", item.contact)
        assertEquals("/rest/v1/indicacoes?select=*&order=criado_em.desc,id.asc", server.takeRequest().path)
    }

    @Test fun memberCannotCreateEditOrDelete() = runTest {
        val client = client()
        val repository = RecommendationRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope)))
        repeat(3) { server.enqueue(MockResponse().setBody("""[{"id":"user","nome":"Membro","role":"membro"}]""")) }
        val draft = RecommendationDraft("Doces", "Descrição válida", "", "")
        try { repository.save(null, draft); fail("Membro criou indicação") } catch (_: SecurityException) {}
        try { repository.save("1", draft); fail("Membro editou indicação") } catch (_: SecurityException) {}
        try { repository.delete("1"); fail("Membro excluiu indicação") } catch (_: SecurityException) {}
        repeat(3) { assertEquals("GET", server.takeRequest().method) }
        assertEquals(3, server.requestCount)
    }

    @Test fun adminCreatesAndEditsTrimmedContent() = runTest {
        val client = client()
        val repository = RecommendationRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope)))
        repeat(2) {
            server.enqueue(MockResponse().setBody("""[{"id":"user","nome":"Admin","role":"admin"}]"""))
            server.enqueue(MockResponse().setResponseCode(204))
        }
        val draft = RecommendationDraft(" Doces ", " Descrição válida ", " Telefone ", " https://example.org ")
        repository.save(null, draft)
        repository.save("1", draft)
        server.takeRequest()
        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("Doces", JSONObject(create.body.readUtf8()).getString("titulo"))
        server.takeRequest()
        val update = server.takeRequest()
        assertEquals("PATCH", update.method)
        assertEquals("/rest/v1/indicacoes?id=eq.1", update.path)
    }

    @Test fun invalidOrExecutableLinksAreRejected() {
        listOf("javascript:alert(1)", "http://example.org", "file:///sdcard/file", "https://user:password@example.org", "https://", "https://example.org/a b").forEach {
            assertFalse(it, safeRecommendationLink(it))
            assertThrows(IllegalArgumentException::class.java) { RecommendationDraft("Doces", "Descrição", "", it).validated() }
        }
        assertTrue(safeRecommendationLink("https://example.org/path?a=1"))
        assertEquals("", RecommendationDraft("Doces", "Descrição", "", "").validated().link)
    }

    private fun client(): SupabaseClient {
        val storage = object : SessionStorage {
            override fun load() = Session("access", "refresh", "user", "user@local.test")
            override fun save(session: Session) {}
            override fun clear() {}
        }
        return SupabaseClient(SupabaseConfig("http://127.0.0.1:${server.port}", "test-key", allowLocalHttp = true), storage, OkHttpClient())
    }
}
