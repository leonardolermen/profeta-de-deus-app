package br.org.profetas.app.study

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import org.json.JSONObject

private const val TABLE = "estudos"

class StudyRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun all(): List<Study> =
        client.rows(TABLE, "select=*&order=criado_em.desc").map { row -> row.toStudy() }

    suspend fun save(id: String?, draft: StudyDraft) {
        guard.requireAdmin()

        client.saveRow(TABLE, id, draft.toRow())
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toStudy(): Study = Study(
        id = getString("id"),
        title = getString("titulo"),
        description = optString("descricao"),
        category = optString("categoria"),
        content = getString("conteudo"),
    )

    private fun StudyDraft.toRow(): JSONObject = JSONObject()
        .put("titulo", title)
        .put("descricao", description)
        .put("categoria", category)
        .put("conteudo", content)
}
