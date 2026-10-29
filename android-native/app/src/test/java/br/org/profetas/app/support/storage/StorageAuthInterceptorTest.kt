package br.org.profetas.app.support.storage

import br.org.profetas.app.support.session.Session
import okhttp3.*
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import java.io.IOException

class StorageAuthInterceptorTest {
    private val server = MockWebServer()
    @Before fun start() { server.start() }
    @After fun stop() { server.shutdown() }
    private val session = Session("access-test", "refresh-test", "member", "member@local.test")

    @Test fun blocksPrivateImageBeforeNetworkWhenLoggedOut() {
        val http = OkHttpClient.Builder().addInterceptor(StorageAuthInterceptor(server.url("/").toString(), "public-key", { null }, {})).build()
        try { http.newCall(Request.Builder().url(server.url("/storage/v1/object/authenticated/galeria/a.jpg")).build()).execute(); fail("Login required") }
        catch (_: IOException) {}
        assertEquals(0, server.requestCount)
    }

    @Test fun replacesLegacyPublicPathAndAttachesCredentials() {
        server.enqueue(MockResponse().setBody("image"))
        val http = OkHttpClient.Builder().addInterceptor(StorageAuthInterceptor(server.url("/").toString(), "public-key", { session }, {})).build()
        http.newCall(Request.Builder().url(server.url("/storage/v1/object/public/galeria/a.jpg")).build()).execute().close()
        val request = server.takeRequest()
        assertEquals("/storage/v1/object/authenticated/galeria/a.jpg", request.path)
        assertEquals("Bearer access-test", request.getHeader("Authorization"))
    }

    @Test fun doesNotAttachCredentialsToUnrelatedResources() {
        server.enqueue(MockResponse().setBody("image"))
        val http = OkHttpClient.Builder().addInterceptor(StorageAuthInterceptor("https://different.invalid", "public-key", { session }, {})).build()
        http.newCall(Request.Builder().url(server.url("/storage/v1/object/authenticated/galeria/a.jpg")).build()).execute().close()
        val request = server.takeRequest()
        assertNull(request.getHeader("Authorization"))
        assertNull(request.getHeader("apikey"))
    }

    @Test fun renewsExpiredImageSessionOnce() {
        var active = session
        var refreshes = 0
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("image"))
        val http = OkHttpClient.Builder().addInterceptor(StorageAuthInterceptor(server.url("/").toString(), "public-key", { active }, {
            refreshes++
            active = session.copy(access = "renewed")
        })).build()
        http.newCall(Request.Builder().url(server.url("/storage/v1/object/authenticated/galeria/a.jpg")).build()).execute().close()
        server.takeRequest()
        assertEquals("Bearer renewed", server.takeRequest().getHeader("Authorization"))
        assertEquals(1, refreshes)
    }
}
