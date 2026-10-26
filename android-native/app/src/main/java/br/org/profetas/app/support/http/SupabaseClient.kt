package br.org.profetas.app.support.http

import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private const val NOT_CONFIGURED_MESSAGE = "O serviço da comunidade ainda não está disponível."

/** Cliente HTTP para a API REST/Auth do Supabase, com renovação de sessão e mensagens de erro em pt-BR. */
class SupabaseClient(
    private val config: SupabaseConfig,
    private val storage: SessionStorage,
    private val http: OkHttpClient,
) {

    private val mutableSessionState = MutableStateFlow(storage.load())

    // Fonte única da sessão: quem precisa reagir a login, logout ou renovação recusada observa este fluxo.
    val sessionState: StateFlow<Session?> = mutableSessionState.asStateFlow()

    val session: Session? get() = mutableSessionState.value

    val isConfigured: Boolean get() = config.isUsable

    private val refreshLock = Mutex()

    suspend fun get(path: String): String = authenticated(path, "GET")

    suspend fun post(path: String, body: JSONObject): String = authenticated(path, "POST", body)

    suspend fun patch(path: String, body: JSONObject): String = authenticated(path, "PATCH", body)

    // GoTrue (Supabase Auth) só aceita PUT para atualizar o usuário; não existe no padrão REST das outras tabelas.
    suspend fun put(path: String, body: JSONObject): String = authenticated(path, "PUT", body)

    suspend fun delete(path: String): String = authenticated(path, "DELETE")

    suspend fun deleteWithBody(path: String, body: JSONObject): String = authenticated(path, "DELETE", body)

    suspend fun upload(path: String, bytes: ByteArray, contentType: String): String {
        check(isConfigured) { NOT_CONFIGURED_MESSAGE }

        val url = config.url.trimEnd('/') + path
        return withSingleRefreshRetry { token -> sendBytes(url, bytes, contentType, token) }
    }

    suspend fun anonymousPost(path: String, body: JSONObject): String =
        send(config.url.trimEnd('/') + path, "POST", body, token = null)

    // Recuperacao de senha chega com um access_token proprio do link, que ainda nao e a sessao guardada.
    suspend fun fetchUser(accessToken: String): JSONObject =
        JSONObject(send(config.url.trimEnd('/') + "/auth/v1/user", token = accessToken))

    // Serviço de terceiros (A Bíblia Digital): sem a apikey do Supabase e com mensagem de erro própria.
    suspend fun external(url: String, token: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url)
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        execute(request) { status, _ -> FailureMessages.forExternal(status) }
    }

    fun adoptSession(json: JSONObject) {
        val user = json.getJSONObject("user")
        adoptSession(
            Session(
                access = json.getString("access_token"),
                refresh = json.getString("refresh_token"),
                userId = user.getString("id"),
                email = user.optString("email"),
            ),
        )
    }

    fun adoptSession(session: Session) {
        mutableSessionState.value = session
        storage.save(session)
    }

    fun dropSession() {
        mutableSessionState.value = null
        storage.clear()
    }

    private suspend fun authenticated(path: String, method: String, body: JSONObject? = null): String {
        check(isConfigured) { NOT_CONFIGURED_MESSAGE }

        val url = config.url.trimEnd('/') + path
        return withSingleRefreshRetry { token -> send(url, method, body, token) }
    }

    // Toda chamada autenticada (REST, PATCH de perfil, upload de foto) renova a sessao uma unica vez
    // quando o servidor responde 401, e repete a mesma chamada com o token novo.
    private suspend fun withSingleRefreshRetry(call: suspend (token: String) -> String): String {
        val previousAccess = session?.access

        try {
            return call(previousAccess ?: config.anonKey)
        } catch (error: ApiException) {
            if (error.status != 401 || session == null) {
                throw error
            }

            refreshCurrentSession(previousAccess)
            return call(session?.access ?: config.anonKey)
        }
    }

    private suspend fun refreshCurrentSession(previousAccess: String?) {
        refreshLock.withLock {
            if (session?.access != previousAccess) {
                // Outra chamada concorrente ja renovou a sessao enquanto esperavamos o lock.
                return
            }

            val refreshToken = requireNotNull(session).refresh
            try {
                val result = send(
                    config.url.trimEnd('/') + "/auth/v1/token?grant_type=refresh_token",
                    "POST",
                    JSONObject().put("refresh_token", refreshToken),
                )
                adoptSession(JSONObject(result))
            } catch (refreshError: ApiException) {
                // 400/401/403 na renovacao: o token de renovacao e invalido, nao ha como recuperar a sessao.
                if (refreshError.status in listOf(400, 401, 403)) {
                    dropSession()
                }
                throw refreshError
            }
        }
    }

    private suspend fun send(
        url: String,
        method: String = "GET",
        body: JSONObject? = null,
        token: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(url)
            .header("Accept", "application/json")
            .header("apikey", config.anonKey)

        token?.takeIf { it.isNotBlank() }?.let { builder.header("Authorization", "Bearer $it") }

        if (method != "GET") {
            val requestBody = if (method == "DELETE" && body == null) {
                null
            } else {
                (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType())
            }
            builder.method(method, requestBody)
        }

        execute(builder.build(), FailureMessages::forSupabase)
    }

    private suspend fun sendBytes(url: String, bytes: ByteArray, contentType: String, token: String): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url)
                .header("Accept", "application/json")
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer $token")
                .header("x-upsert", "false")
                .method("POST", bytes.toRequestBody(contentType.toMediaType()))
                .build()

            execute(request, FailureMessages::forSupabase)
        }

    private fun execute(request: Request, failureMessage: (status: Int, body: String) -> String): String {
        try {
            http.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw ApiException(response.code, failureMessage(response.code, text))
                }
                return text
            }
        } catch (e: IOException) {
            throw ConnectionException(e)
        }
    }
}
