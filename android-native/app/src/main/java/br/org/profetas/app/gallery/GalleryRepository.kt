package br.org.profetas.app.gallery

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import br.org.profetas.app.support.http.textOrNull
import org.json.JSONObject

private const val TABLE = "fotos"

class GalleryRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun all(): List<Photo> =
        client.rows(TABLE, "select=*&order=criado_em.desc").map { row -> row.toPhoto() }

    suspend fun save(id: String?, draft: PhotoDraft) {
        guard.requireAdmin()

        client.saveRow(TABLE, id, draft.toRow())
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toPhoto(): Photo = Photo(
        id = getString("id"),
        title = getString("titulo"),
        album = optString("album"),
        url = textOrNull("url"),
        path = textOrNull("caminho"),
    )

    private fun PhotoDraft.toRow(): JSONObject = JSONObject()
        .put("titulo", title)
        .put("url", url)
        .put("album", album)
}
