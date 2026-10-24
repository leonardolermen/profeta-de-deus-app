package br.org.profetas.app.event

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class NoSessionStorage : SessionStorage {

    override fun load(): Session? = null

    override fun save(session: Session) {
    }

    override fun clear() {
    }
}

class EventRepositoryTest {

    private val server = MockWebServer()

    @Before fun startServer() {
        server.start()
    }

    @After fun stopServer() {
        server.shutdown()
    }

    // 127.0.0.1 com allowLocalHttp é o caminho do Supabase local em debug: evita montar https só para o teste.
    private fun repository(scope: CoroutineScope): EventRepository {
        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, NoSessionStorage(), OkHttpClient())

        return EventRepository(client, ContentWriteGuard(AccountRepository(client, scope)))
    }

    @Test fun readsImagePathAndType() = runTest {
        val repository = repository(backgroundScope)
        server.enqueue(
            MockResponse().setBody(
                """[{"id":"1","titulo":"T","descricao":null,"data_evento":"2026-06-25T10:00:00-03:00",""" +
                    """"tipo":"jovens","imagem":"2026/a.jpg"}]""",
            ),
        )
        server.enqueue(
            MockResponse().setBody(
                """[{"id":"2","titulo":"T","descricao":"D","data_evento":"2026-06-25T10:00:00-03:00",""" +
                    """"tipo":"culto","imagem":null}]""",
            ),
        )

        val withImage = repository.all().single()
        val withoutImage = repository.all().single()

        val expected = Event(
            id = "1",
            title = "T",
            description = "",
            date = "2026-06-25T10:00:00-03:00",
            type = EventType.JOVENS,
            imagePath = "2026/a.jpg",
        )
        assertEquals(expected, withImage)
        assertNull(withoutImage.imagePath)
        assertEquals("/rest/v1/eventos?select=*&order=data_evento.asc", server.takeRequest().path)
    }
}
