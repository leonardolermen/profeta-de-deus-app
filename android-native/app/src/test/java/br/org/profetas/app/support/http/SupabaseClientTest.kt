package br.org.profetas.app.support.http

import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.InetAddress

private class InMemorySessionStorage(private var current: Session? = null) : SessionStorage {

    var cleared = false
    var lastSaved: Session? = null

    override fun load(): Session? = current

    override fun save(session: Session) {
        current = session
        lastSaved = session
    }

    override fun clear() {
        current = null
        cleared = true
    }
}

class SupabaseClientTest {

    // isSecureUrl exige https; o MockWebServer precisa de um certificado descartável para falar https de verdade.
    // O nome usado na URL do MockWebServer vem de uma resolução reversa do loopback, que varia por máquina
    // (em Docker/Windows costuma virar "kubernetes.docker.internal"), então o certificado cobre os dois nomes.
    private val localhostCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .addSubjectAlternativeName(InetAddress.getLoopbackAddress().canonicalHostName)
        .build()
    private val serverCertificates = HandshakeCertificates.Builder().heldCertificate(localhostCertificate).build()
    private val clientCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localhostCertificate.certificate)
        .build()

    private val server = MockWebServer()
    private val http = OkHttpClient.Builder()
        .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
        .build()

    @Before fun startServer() {
        server.useHttps(serverCertificates.sslSocketFactory(), false)
        server.start()
    }

    @After fun stopServer() {
        server.shutdown()
    }

    private fun config() =
        SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon-key", allowLocalHttp = false)

    @Test fun sendsApiKeyAndBearerOfCurrentSession() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        server.enqueue(MockResponse().setBody("[]"))

        client.get("/rest/v1/eventos")

        val request = server.takeRequest()
        assertEquals("anon-key", request.getHeader("apikey"))
        assertEquals("Bearer A", request.getHeader("Authorization"))
    }

    @Test fun refreshesOnceOn401AndRetries() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse().setBody(
                """{"access_token":"B","refresh_token":"R2","user":{"id":"u","email":"e"}}""",
            ),
        )
        server.enqueue(MockResponse().setBody("[]"))

        val result = client.get("/rest/v1/eventos")

        assertEquals("[]", result)
        server.takeRequest()
        server.takeRequest()
        val retry = server.takeRequest()
        assertEquals("Bearer B", retry.getHeader("Authorization"))
        assertEquals("R2", storage.lastSaved?.refresh)
    }

    @Test fun failedRefreshDropsSession() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(400))

        try {
            client.get("/rest/v1/eventos")
            fail("deveria lançar ApiException")
        } catch (error: ApiException) {
            assertEquals(400, error.status)
        }

        assertNull(client.session)
        assertTrue(storage.cleared)
    }

    @Test fun sessionStateClearsWhenRefreshFails() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(400))

        runCatching { client.get("/rest/v1/eventos") }

        assertNull(client.sessionState.value)
    }

    @Test fun mapsStatusToFriendlyMessage() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)

        server.enqueue(MockResponse().setResponseCode(403))
        val forbidden = runCatching { client.get("/rest/v1/eventos") }.exceptionOrNull() as ApiException
        assertEquals("Você não tem permissão para esta ação.", forbidden.message)

        server.enqueue(MockResponse().setResponseCode(409))
        val conflict = runCatching { client.get("/rest/v1/eventos") }.exceptionOrNull() as ApiException
        assertEquals("Este cadastro já existe.", conflict.message)

        server.enqueue(MockResponse().setResponseCode(429))
        val tooMany = runCatching { client.get("/rest/v1/eventos") }.exceptionOrNull() as ApiException
        assertEquals("Muitas tentativas. Aguarde e tente novamente.", tooMany.message)
    }

    @Test fun mapsAuthErrorCodesToPortuguese() = runTest {
        val client = SupabaseClient(config(), InMemorySessionStorage(null), http)
        val cases = listOf(
            Triple("invalid_credentials", 400, "E-mail ou senha incorretos."),
            Triple("user_already_exists", 422, "Já existe uma conta com este e-mail."),
            Triple("weak_password", 422, "Senha fraca. Escolha uma senha mais forte."),
            Triple("email_not_confirmed", 400, "Confirme seu e-mail antes de entrar."),
            Triple("same_password", 422, "A nova senha precisa ser diferente da atual."),
            Triple(
                "over_email_send_rate_limit",
                429,
                "Muitos e-mails enviados. Aguarde alguns minutos e tente novamente.",
            ),
        )

        for ((code, status, expected) in cases) {
            server.enqueue(
                MockResponse().setResponseCode(status)
                    .setBody("""{"code":$status,"error_code":"$code","msg":"English server text"}"""),
            )

            val error = runCatching { client.anonymousPost("/auth/v1/token", JSONObject()) }.exceptionOrNull()

            assertEquals(code, expected, (error as ApiException).message)
            assertEquals(code, status, error.status)
        }
    }

    @Test fun readsAuthErrorCodeFromCodeField() = runTest {
        val client = SupabaseClient(config(), InMemorySessionStorage(null), http)
        server.enqueue(
            MockResponse().setResponseCode(400)
                .setBody("""{"code":"invalid_credentials","message":"Invalid login credentials"}"""),
        )

        val error = runCatching { client.anonymousPost("/auth/v1/token", JSONObject()) }.exceptionOrNull()

        assertEquals("E-mail ou senha incorretos.", error?.message)
    }

    @Test fun neverForwardsServerTextForUnknownFailures() = runTest {
        val client = SupabaseClient(config(), InMemorySessionStorage(null), http)
        server.enqueue(
            MockResponse().setResponseCode(400)
                .setBody("""{"msg":"Invalid login credentials","error_description":"x","message":"y"}"""),
        )
        server.enqueue(MockResponse().setResponseCode(500).setBody("upstream timeout"))

        val unknownCode = runCatching { client.anonymousPost("/auth/v1/token", JSONObject()) }.exceptionOrNull()
        val notJson = runCatching { client.anonymousPost("/auth/v1/token", JSONObject()) }.exceptionOrNull()

        assertEquals("Falha na solicitação (400).", unknownCode?.message)
        assertEquals("Falha na solicitação (500).", notJson?.message)
    }

    @Test fun connectionFailureBecomesPortugueseMessageAndIsNotApiException() = runTest {
        val client = SupabaseClient(config(), InMemorySessionStorage(Session("A", "R", "u", "e")), http)
        server.shutdown()

        val error = runCatching { client.get("/rest/v1/eventos") }.exceptionOrNull()

        assertTrue(error is IOException)
        assertFalse(error is ApiException)
        assertEquals("Sem conexão com a internet. Verifique a rede e tente novamente.", error?.message)
        assertTrue(error?.cause is IOException)
    }

    @Test fun refreshFinishingAfterLogoutDoesNotRestoreTheSession() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().startsWith("/auth/v1/token") -> {
                    // O usuário toca em "Sair" enquanto a renovação ainda está em andamento.
                    client.dropSession()
                    MockResponse().setBody(
                        """{"access_token":"B","refresh_token":"R2","user":{"id":"u","email":"e"}}""",
                    )
                }
                request.getHeader("Authorization") == "Bearer A" -> MockResponse().setResponseCode(401)
                else -> MockResponse().setBody("[]")
            }
        }

        runCatching { client.get("/rest/v1/eventos") }

        assertNull(client.sessionState.value)
        assertNull(storage.load())
    }

    @Test fun uploadSendsRawBytesWithContentType() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        val bytes = byteArrayOf(1, 2, 3, 4)
        server.enqueue(MockResponse().setBody("{}"))

        client.upload("/storage/v1/object/eventos/a.jpg", bytes, "image/jpeg")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("image/jpeg", request.getHeader("Content-Type"))
        assertEquals(bytes.toList(), request.body.readByteArray().toList())
    }

    @Test fun uploadRefreshesOn401AndRetries() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        val bytes = byteArrayOf(1, 2, 3, 4)
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse().setBody(
                """{"access_token":"B","refresh_token":"R2","user":{"id":"u","email":"e"}}""",
            ),
        )
        server.enqueue(MockResponse().setBody("{}"))

        client.upload("/storage/v1/object/eventos/a.jpg", bytes, "image/jpeg")

        server.takeRequest()
        server.takeRequest()
        val retry = server.takeRequest()
        assertEquals("Bearer B", retry.getHeader("Authorization"))
        assertEquals(bytes.toList(), retry.body.readByteArray().toList())
    }

    @Test fun uploadFailedRefreshDropsSession() = runTest {
        val storage = InMemorySessionStorage(Session("A", "R", "u", "e"))
        val client = SupabaseClient(config(), storage, http)
        val bytes = byteArrayOf(1, 2, 3, 4)
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(400))

        try {
            client.upload("/storage/v1/object/eventos/a.jpg", bytes, "image/jpeg")
            fail("deveria lançar ApiException")
        } catch (error: ApiException) {
            assertEquals(400, error.status)
        }

        assertNull(client.session)
        assertTrue(storage.cleared)
    }

    @Test fun refusesWhenNotConfigured() = runTest {
        val storage = InMemorySessionStorage(null)
        val config = SupabaseConfig("http://" + server.hostName, "anon-key", allowLocalHttp = false)
        val client = SupabaseClient(config, storage, http)

        val error = runCatching { client.get("/rest/v1/eventos") }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertEquals("O serviço da comunidade ainda não está disponível.", error?.message)
    }
}
