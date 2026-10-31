package br.org.profetas.app.recommendation

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import org.json.JSONObject
import java.net.URI

data class Recommendation(val id: String, val title: String, val description: String, val contact: String, val link: String)

data class RecommendationDraft(val title: String, val description: String, val contact: String, val link: String) {
    fun validated(): RecommendationDraft {
        val result = RecommendationDraft(title.trim(), description.trim(), contact.trim(), link.trim())
        require(result.title.length in 2..160) { "Informe um título entre 2 e 160 caracteres." }
        require(result.description.length in 5..4000) { "Descreva a indicação (5 a 4000 caracteres)." }
        require(result.contact.length <= 200) { "O contato deve ter até 200 caracteres." }
        require(result.link.length <= 2048 && (result.link.isEmpty() || safeRecommendationLink(result.link))) {
            "Informe um link completo começando com https:// ou deixe vazio."
        }
        return result
    }
}

fun safeRecommendationLink(link: String): Boolean = runCatching {
    val uri = URI(link)
    uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null
}.getOrDefault(false)

class RecommendationRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {
    suspend fun all(): List<Recommendation> = client.rows("indicacoes", "select=*&order=criado_em.desc,id.asc").map {
        Recommendation(it.getString("id"), it.getString("titulo"), it.getString("descricao"),
            it.optString("contato"), it.optString("link"))
    }

    suspend fun save(id: String?, draft: RecommendationDraft) {
        guard.requireAdmin()
        val valid = draft.validated()
        client.saveRow("indicacoes", id, JSONObject().put("titulo", valid.title).put("descricao", valid.description)
            .put("contato", valid.contact).put("link", valid.link))
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()
        client.deleteRow("indicacoes", id)
    }
}
