package br.org.profetas.app.support.storage

import android.net.Uri
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.net.InetAddress
import java.time.Year

private class FixedSessionStorage : SessionStorage {

    private var current: Session? = Session("A", "R", "u", "e")

    override fun load(): Session? = current

    override fun save(session: Session) {
        current = session
    }

    override fun clear() {
        current = null
    }
}

private class FixedBytesReader(private val bytes: ByteArray) : ImageReader {

    override suspend fun readJpeg(source: Uri, maxSide: Int, quality: Int): ByteArray = bytes
}

class ImageUploaderTest {

    // Mesmo esquema do SupabaseClientTest: o cliente exige https, então o servidor usa um certificado descartável.
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

    private fun uploader(bytes: ByteArray = byteArrayOf(9, 8, 7)): ImageUploader {
        val config = SupabaseConfig(server.url("/").toString().trimEnd('/'), "anon-key", allowLocalHttp = false)

        return ImageUploader(SupabaseClient(config, FixedSessionStorage(), http), FixedBytesReader(bytes))
    }

    @Test fun uploadsJpegUnderYearFolder() = runTest {
        val bytes = byteArrayOf(9, 8, 7)
        server.enqueue(MockResponse().setBody("{}"))

        val path = uploader(bytes).upload(StorageBucket.EVENTS, mock(Uri::class.java))

        val year = Year.now().value
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val expectedPath = Regex("/storage/v1/object/eventos/$year/[0-9a-f-]{36}[.]jpg")
        assertTrue(request.path!!.matches(expectedPath))
        assertEquals("image/jpeg", request.getHeader("Content-Type"))
        assertEquals(bytes.toList(), request.body.readByteArray().toList())
        assertTrue(path.startsWith("$year/"))
        assertTrue(request.path!!.endsWith("/$path"))
    }

    @Test fun removeSendsPrefixes() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        uploader().remove(StorageBucket.EVENTS, "2026/a.jpg")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/storage/v1/object/eventos", request.path)
        assertEquals("""{"prefixes":["2026/a.jpg"]}""", request.body.readUtf8())
    }
}
