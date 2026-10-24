package br.org.profetas.app.account

import androidx.core.net.toUri
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.textOrEmpty
import br.org.profetas.app.support.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.json.JSONObject

class AccountRepository(private val client: SupabaseClient, scope: CoroutineScope) {

    private val loadedProfile = MutableStateFlow<Profile?>(null)

    // Derivados da sessão do SupabaseClient: uma renovação recusada em qualquer repositório derruba a sessão lá,
    // e o perfil carregado (talvez de admin) deixa de valer junto, sem depender de quem fez a chamada.
    val isLoggedIn: StateFlow<Boolean> = client.sessionState
        .map { session -> session != null }
        .stateIn(scope, SharingStarted.Eagerly, client.session != null)

    val currentProfile: StateFlow<Profile?> = combine(loadedProfile, client.sessionState) { profile, session ->
        profile?.takeIf { loaded -> loaded.id == session?.userId }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val session: Session? get() = client.session
    val isConfigured: Boolean get() = client.isConfigured

    suspend fun login(email: String, password: String) {
        check(isConfigured) { "O acesso às contas ainda não está disponível." }

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
        client.adoptSession(JSONObject(client.anonymousPost("/auth/v1/token?grant_type=password", body)))
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        check(isConfigured) { "O cadastro de contas ainda não está disponível." }

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", JSONObject().put("nome", name.trim()))
        val result = JSONObject(client.anonymousPost("/auth/v1/signup", body))

        val loggedIn = result.optString("access_token").isNotBlank()
        if (loggedIn) {
            client.adoptSession(result)
        }

        return loggedIn
    }

    suspend fun requestRecovery(email: String) {
        val path = "/auth/v1/recover?redirect_to=profetas%3A%2F%2Fauth%2Frecovery"
        client.post(path, JSONObject().put("email", email.trim()))
    }

    suspend fun acceptRecovery(link: String) {
        val uri = link.toUri()
        val isRecoveryLink = uri.scheme == "profetas" && uri.host == "auth" && uri.path == "/recovery"
        require(isRecoveryLink) { "Link de recuperação inválido." }

        val parameters = ("https://localhost/?" + uri.fragment.orEmpty()).toUri()
        require(parameters.getQueryParameter("type") == "recovery") { "Link de recuperação inválido ou expirado." }

        val access = requireNotNull(parameters.getQueryParameter("access_token"))
        val refresh = requireNotNull(parameters.getQueryParameter("refresh_token"))
        check(isConfigured) { "A recuperação de conta ainda não está disponível." }

        val user = client.fetchUser(access)
        client.adoptSession(Session(access, refresh, user.getString("id"), user.optString("email")))
    }

    suspend fun updatePassword(password: String) {
        client.put("/auth/v1/user", JSONObject().put("password", password))
    }

    suspend fun logout() {
        try {
            if (isConfigured && session != null) {
                client.post("/auth/v1/logout", JSONObject())
            }
        } finally {
            client.dropSession()
            loadedProfile.value = null
        }
    }

    suspend fun profile(): Profile? {
        val profile = session?.let { activeSession ->
            client.rows("perfis", "select=*&id=eq.${activeSession.userId}").firstOrNull()?.toProfile()
        }

        loadedProfile.value = profile

        return profile
    }

    suspend fun saveName(name: String) {
        val id = requireNotNull(session).userId
        client.patch("/rest/v1/perfis?id=eq.$id", JSONObject().put("nome", name.trim()))
    }

    private fun JSONObject.toProfile(): Profile = Profile(
        id = getString("id"),
        name = textOrEmpty("nome"),
        role = getString("role"),
    )
}
