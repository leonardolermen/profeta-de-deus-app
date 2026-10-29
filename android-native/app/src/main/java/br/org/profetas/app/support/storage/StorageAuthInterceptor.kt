package br.org.profetas.app.support.storage

import br.org.profetas.app.support.session.Session
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Credenciais somente para os buckets do próprio projeto; nunca para fotos externas ou redirects. */
class StorageAuthInterceptor(
    baseUrl: String,
    private val apiKey: String,
    private val session: () -> Session?,
    private val refresh: suspend (String?) -> Unit,
) : Interceptor {
    private val origin = baseUrl.toHttpUrl()
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        val trusted = url.scheme == origin.scheme && url.host == origin.host && url.port == origin.port
        val path = url.encodedPath
        val isPhoto = listOf("eventos", "galeria").any { bucket ->
            path.startsWith("/storage/v1/object/authenticated/$bucket/") || path.startsWith("/storage/v1/object/public/$bucket/")
        }
        if (!trusted || !isPhoto) return chain.proceed(original)
        val active = session() ?: throw IOException("Entre na sua conta para visualizar as fotos.")
        val authenticatedUrl = url.newBuilder().encodedPath(path.replace("/object/public/", "/object/authenticated/")).build()
        fun request(token: String) = original.newBuilder().url(authenticatedUrl)
            .header("Authorization", "Bearer $token").header("apikey", apiKey).header("Cache-Control", "no-store").build()
        var response = chain.proceed(request(active.access))
        if (response.code == 401) {
            response.close()
            runBlocking { refresh(active.access) }
            val renewed = session() ?: throw IOException("Sua sessão expirou. Entre novamente.")
            response = chain.proceed(request(renewed.access))
        }
        return response
    }
}
