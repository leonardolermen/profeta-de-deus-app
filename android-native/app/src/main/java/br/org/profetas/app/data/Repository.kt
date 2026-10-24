package br.org.profetas.app.data

import android.content.Context
import android.net.Uri
import br.org.profetas.app.BuildConfig
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.session.KeystoreSessionStorage
import br.org.profetas.app.support.session.Session
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key)

class Repository(context: Context) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val client = SupabaseClient(SupabaseConfig.fromBuildConfig(), KeystoreSessionStorage(context), http)

    val session: Session? get() = client.session
    val configured: Boolean get() = client.isConfigured

    suspend fun login(email: String, password: String) {
        check(configured) { "O acesso às contas ainda não está disponível." }

        val body = JSONObject().put("email", email.trim()).put("password", password)
        client.adoptSession(JSONObject(client.anonymousPost("/auth/v1/token?grant_type=password", body)))
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        check(configured) { "O cadastro de contas ainda não está disponível." }

        val body = JSONObject().put("email", email.trim()).put("password", password)
            .put("data", JSONObject().put("nome", name.trim()))
        val result = JSONObject(client.anonymousPost("/auth/v1/signup", body))

        val loggedIn = result.optString("access_token").isNotBlank()
        if (loggedIn) {
            client.adoptSession(result)
        }
        return loggedIn
    }

    suspend fun resetPassword(email: String) {
        val path = "/auth/v1/recover?redirect_to=profetas%3A%2F%2Fauth%2Frecovery"
        client.post(path, JSONObject().put("email", email.trim()))
    }

    suspend fun acceptRecovery(link: String) {
        val uri = Uri.parse(link)
        val isRecoveryLink = uri.scheme == "profetas" && uri.host == "auth" && uri.path == "/recovery"
        require(isRecoveryLink) { "Link de recuperação inválido." }

        val params = Uri.parse("https://localhost/?" + uri.fragment.orEmpty())
        require(params.getQueryParameter("type") == "recovery") { "Link de recuperação inválido ou expirado." }

        val access = requireNotNull(params.getQueryParameter("access_token"))
        val refresh = requireNotNull(params.getQueryParameter("refresh_token"))
        check(configured) { "A recuperação de conta ainda não está disponível." }

        val user = client.fetchUser(access)
        client.adoptSession(Session(access, refresh, user.getString("id"), user.optString("email")))
    }

    suspend fun updatePassword(password: String) {
        client.put("/auth/v1/user", JSONObject().put("password", password))
    }

    suspend fun logout() {
        try {
            if (configured && session != null) {
                client.post("/auth/v1/logout", JSONObject())
            }
        } finally {
            client.dropSession()
        }
    }

    private suspend fun rows(table: String, query: String = "select=*"): List<JSONObject> {
        val array = JSONArray(client.get("/rest/v1/$table?$query"))
        return (0 until array.length()).map { array.getJSONObject(it) }
    }

    suspend fun events() = rows("eventos", "select=*&order=data_evento.asc").map {
        Event(
            it.getString("id"), it.getString("titulo"), it.text("descricao"),
            it.getString("data_evento"), it.getString("tipo"),
        )
    }

    suspend fun studies() = rows("estudos", "select=*&order=criado_em.desc").map {
        Study(
            it.getString("id"), it.getString("titulo"), it.optString("descricao"),
            it.optString("categoria"), it.getString("conteudo"),
        )
    }

    suspend fun birthdays() = rows("aniversariantes", "select=*&order=dia.asc").map {
        Birthday(
            it.getString("id"), it.getString("nome"), it.getInt("dia"),
            it.getInt("mes"), it.getBoolean("consentimento"),
        )
    }

    suspend fun photos() = rows("fotos", "select=*&order=criado_em.desc").map {
        Photo(it.getString("id"), it.getString("titulo"), it.getString("url"), it.optString("album"))
    }

    suspend fun church(): Church = rows("igreja").firstOrNull()?.let {
        Church(
            it.getString("nome"), it.optString("telefone"), it.optString("whatsapp"), it.optString("email"),
            it.optString("endereco"), it.optString("historia"), it.optString("privacidade"),
        )
    } ?: Church()

    suspend fun profile(): Profile? = session?.let { currentSession ->
        rows("perfis", "select=*&id=eq.${currentSession.userId}").firstOrNull()
            ?.let { Profile(it.getString("id"), it.text("nome"), it.getString("role")) }
    }

    suspend fun saveProfile(name: String) {
        val id = requireNotNull(session).userId
        client.patch("/rest/v1/perfis?id=eq.$id", JSONObject().put("nome", name.trim()))
    }

    suspend fun pray(name: String, text: String) {
        client.post("/rest/v1/oracoes", JSONObject().put("nome", name.trim()).put("pedido", text.trim()))
    }

    suspend fun prayers() = rows("oracoes", "select=*&order=criado_em.desc").map {
        Prayer(it.getString("id"), it.getString("nome"), it.getString("pedido"), it.getString("criado_em"))
    }

    suspend fun registrations() = rows("inscricoes", "select=*&order=criado_em.desc").map {
        Registration(
            it.getString("id"), it.getString("evento_id"), it.getString("nome"),
            it.getString("telefone"), it.getString("usuario_id"),
        )
    }

    suspend fun enroll(eventId: String, name: String, phone: String) {
        val body = JSONObject().put("evento_id", eventId).put("usuario_id", requireNotNull(session).userId)
            .put("nome", name.trim()).put("telefone", phone.trim())
        client.post("/rest/v1/inscricoes", body)
    }

    suspend fun removeRegistration(id: String) {
        client.delete("/rest/v1/inscricoes?id=eq.$id")
    }

    private suspend fun requireContentAdmin() {
        // Reconsulta o cargo: nao aceita permissao antiga se o acesso foi revogado.
        // A RLS do banco continua sendo a autoridade, inclusive para chamadas externas ao app.
        ContentAccess.requireAdmin(profile())
    }

    suspend fun saveContent(table: String, id: String?, body: JSONObject) {
        require(table in listOf("eventos", "estudos", "fotos", "aniversariantes", "igreja"))
        requireContentAdmin()

        val path = "/rest/v1/$table" + (id?.let { "?id=eq.$it" } ?: "")
        if (id == null) {
            client.post(path, body)
        } else {
            client.patch(path, body)
        }
    }

    suspend fun deleteContent(table: String, id: String) {
        require(table in listOf("eventos", "estudos", "fotos", "aniversariantes", "oracoes", "inscricoes"))
        requireContentAdmin()
        client.delete("/rest/v1/$table?id=eq.$id")
    }

    private suspend fun bible(path: String): String =
        client.external("https://abibliadigital.api.br/api/$path", BuildConfig.BIBLE_API_TOKEN)

    suspend fun books(): List<Book> {
        val array = JSONArray(bible("books"))
        return (0 until array.length()).map {
            val book = array.getJSONObject(it)
            Book(book.getString("name"), book.getJSONObject("abbrev").getString("pt"), book.getInt("chapters"))
        }
    }

    suspend fun chapter(book: Book, number: Int): Chapter {
        val json = JSONObject(bible("verses/nvi/${book.abbrev}/$number"))
        val verses = json.getJSONArray("verses")
        return Chapter(
            book.name,
            number,
            (0 until verses.length()).map {
                val verse = verses.getJSONObject(it)
                Verse(verse.getInt("number"), verse.getString("text"))
            },
        )
    }
}
