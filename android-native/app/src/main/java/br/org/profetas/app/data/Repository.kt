package br.org.profetas.app.data

import android.content.Context
import br.org.profetas.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiException(val status: Int, message: String) : Exception(message)
private fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key)
class Repository(context: Context) {
    private val store = SessionStore(context)
    @Volatile var session: Session? = store.load(); private set
    val configured get() = validHttps(BuildConfig.SUPABASE_URL) && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    private val refreshLock = Mutex()
    private val http = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(25, TimeUnit.SECONDS).build()
    private suspend fun send(url: String, method: String = "GET", body: JSONObject? = null, token: String? = null, supabase: Boolean = true): String = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url(url).header("Accept", "application/json")
        if (supabase) builder.header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        token?.takeIf { it.isNotBlank() }?.let { builder.header("Authorization", "Bearer $it") }
        if (method != "GET") builder.method(method, if (method == "DELETE") null else (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType()))
        http.newCall(builder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = when (response.code) {
                    401 -> "Sua sessão expirou. Entre novamente."
                    403 -> "Você não tem permissão para esta ação."
                    409 -> "Este cadastro já existe."
                    429 -> "Muitas tentativas. Aguarde e tente novamente."
                    else -> if (!supabase) "Não foi possível consultar a Bíblia (${response.code})." else
                        runCatching { JSONObject(text).let { it.optString("msg", it.optString("error_description", it.optString("message", "Falha na solicitação."))) } }.getOrDefault("Falha na solicitação (${response.code}).")
                }
                throw ApiException(response.code, message)
            }
            text
        }
    }
    private fun saveSession(json: JSONObject) {
        val user = json.getJSONObject("user")
        session = Session(json.getString("access_token"), json.getString("refresh_token"), user.getString("id"), user.optString("email"))
        store.save(session)
    }
    private suspend fun request(path: String, method: String = "GET", body: JSONObject? = null): String {
        check(configured) { "O serviço da comunidade ainda não está disponível." }
        val previous = session?.access
        val url = BuildConfig.SUPABASE_URL.trimEnd('/') + path
        try { return send(url, method, body, previous ?: BuildConfig.SUPABASE_ANON_KEY) }
        catch (error: ApiException) {
            if (error.status != 401 || session == null) throw error
            refreshLock.withLock {
                if (session?.access == previous) {
                    try {
                        val result = send(BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/token?grant_type=refresh_token", "POST", JSONObject().put("refresh_token", session!!.refresh))
                        saveSession(JSONObject(result))
                    } catch (refreshError: ApiException) {
                        if (refreshError.status in listOf(400, 401, 403)) { session = null; store.save(null) }
                        throw refreshError
                    }
                }
            }
            return send(url, method, body, session?.access ?: BuildConfig.SUPABASE_ANON_KEY)
        }
    }
    suspend fun login(email: String, password: String) {
        check(configured) { "O acesso às contas ainda não está disponível." }
        saveSession(JSONObject(send(BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/token?grant_type=password", "POST", JSONObject().put("email", email.trim()).put("password", password))))
    }
    suspend fun register(name: String, email: String, password: String): Boolean {
        check(configured) { "O cadastro de contas ainda não está disponível." }
        val result = JSONObject(send(BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/signup", "POST", JSONObject().put("email", email.trim()).put("password", password).put("data", JSONObject().put("nome", name.trim()))))
        val logged = result.optString("access_token").isNotBlank()
        if (logged) saveSession(result)
        return logged
    }
    suspend fun resetPassword(email: String) { request("/auth/v1/recover?redirect_to=profetas%3A%2F%2Fauth%2Frecovery", "POST", JSONObject().put("email", email.trim())) }
    suspend fun acceptRecovery(link: String) {
        val uri = android.net.Uri.parse(link)
        require(uri.scheme == "profetas" && uri.host == "auth" && uri.path == "/recovery") { "Link de recuperação inválido." }
        val params = android.net.Uri.parse("https://localhost/?" + uri.fragment.orEmpty())
        require(params.getQueryParameter("type") == "recovery") { "Link de recuperação inválido ou expirado." }
        val access = requireNotNull(params.getQueryParameter("access_token"))
        val refresh = requireNotNull(params.getQueryParameter("refresh_token"))
        check(configured) { "A recuperação de conta ainda não está disponível." }
        val user = JSONObject(send(BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/user", token=access))
        session = Session(access,refresh,user.getString("id"),user.optString("email"))
        store.save(session)
    }
    suspend fun updatePassword(password: String) { request("/auth/v1/user", "PUT", JSONObject().put("password", password)) }
    suspend fun logout() {
        try { if (configured && session != null) request("/auth/v1/logout", "POST") }
        finally { session = null; store.save(null) }
    }
    private suspend fun rows(table: String, query: String = "select=*"): List<JSONObject> {
        val array = JSONArray(request("/rest/v1/$table?$query"))
        return (0 until array.length()).map { array.getJSONObject(it) }
    }
    suspend fun events() = rows("eventos", "select=*&order=data_evento.asc").map { Event(it.getString("id"), it.getString("titulo"), it.text("descricao"), it.getString("data_evento"), it.getString("tipo")) }
    suspend fun studies() = rows("estudos", "select=*&order=criado_em.desc").map { Study(it.getString("id"), it.getString("titulo"), it.optString("descricao"), it.optString("categoria"), it.getString("conteudo")) }
    suspend fun birthdays() = rows("aniversariantes", "select=*&order=dia.asc").map { Birthday(it.getString("id"), it.getString("nome"), it.getInt("dia"), it.getInt("mes"), it.getBoolean("consentimento")) }
    suspend fun photos() = rows("fotos", "select=*&order=criado_em.desc").map { Photo(it.getString("id"), it.getString("titulo"), it.getString("url"), it.optString("album")) }
    suspend fun church(): Church = rows("igreja").firstOrNull()?.let { Church(it.getString("nome"), it.optString("telefone"), it.optString("whatsapp"), it.optString("email"), it.optString("endereco"), it.optString("historia"), it.optString("privacidade")) } ?: Church()
    suspend fun profile(): Profile? = session?.let { s -> rows("perfis", "select=*&id=eq.${s.userId}").firstOrNull()?.let { Profile(it.getString("id"), it.text("nome"), it.getString("role")) } }
    suspend fun saveProfile(name: String) { val id = requireNotNull(session).userId; request("/rest/v1/perfis?id=eq.$id", "PATCH", JSONObject().put("nome", name.trim())) }
    suspend fun pray(name: String, text: String) { request("/rest/v1/oracoes", "POST", JSONObject().put("nome", name.trim()).put("pedido", text.trim())) }
    suspend fun prayers() = rows("oracoes", "select=*&order=criado_em.desc").map { Prayer(it.getString("id"), it.getString("nome"), it.getString("pedido"), it.getString("criado_em")) }
    suspend fun registrations() = rows("inscricoes", "select=*&order=criado_em.desc").map { Registration(it.getString("id"), it.getString("evento_id"), it.getString("nome"), it.getString("telefone"), it.getString("usuario_id")) }
    suspend fun enroll(eventId: String, name: String, phone: String) {
        request("/rest/v1/inscricoes", "POST", JSONObject().put("evento_id", eventId).put("usuario_id", requireNotNull(session).userId).put("nome", name.trim()).put("telefone", phone.trim()))
    }
    suspend fun removeRegistration(id: String) { request("/rest/v1/inscricoes?id=eq.$id", "DELETE") }
    private suspend fun requireContentAdmin() {
        // Reconsulta o cargo: nao aceita permissao antiga se o acesso foi revogado.
        // A RLS do banco continua sendo a autoridade, inclusive para chamadas externas ao app.
        ContentAccess.requireAdmin(profile())
    }
    suspend fun saveContent(table: String, id: String?, body: JSONObject) {
        require(table in listOf("eventos", "estudos", "fotos", "aniversariantes", "igreja"))
        requireContentAdmin()
        request("/rest/v1/$table" + (id?.let { "?id=eq.$it" } ?: ""), if (id == null) "POST" else "PATCH", body)
    }
    suspend fun deleteContent(table: String, id: String) {
        require(table in listOf("eventos", "estudos", "fotos", "aniversariantes", "oracoes", "inscricoes"))
        requireContentAdmin()
        request("/rest/v1/$table?id=eq.$id", "DELETE")
    }
    private suspend fun bible(path: String): String = send("https://abibliadigital.api.br/api/$path", token = BuildConfig.BIBLE_API_TOKEN, supabase = false)
    suspend fun books(): List<Book> {
        val array = JSONArray(bible("books"))
        return (0 until array.length()).map { array.getJSONObject(it).let { b -> Book(b.getString("name"), b.getJSONObject("abbrev").getString("pt"), b.getInt("chapters")) } }
    }
    suspend fun chapter(book: Book, number: Int): Chapter {
        val json = JSONObject(bible("verses/nvi/${book.abbrev}/$number"))
        val verses = json.getJSONArray("verses")
        return Chapter(book.name, number, (0 until verses.length()).map { verses.getJSONObject(it).let { v -> Verse(v.getInt("number"), v.getString("text")) } })
    }
}
