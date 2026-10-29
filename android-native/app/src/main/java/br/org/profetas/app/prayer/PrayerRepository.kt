package br.org.profetas.app.prayer

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import org.json.JSONObject

private const val TABLE = "oracoes"

class PrayerRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun send(name: String, text: String) {
        val body = JSONObject()
            .put("nome", name.trim())
            .put("pedido", text.trim())

        client.post("/rest/v1/$TABLE", body)
    }

    suspend fun all(): List<Prayer> {
        guard.requireAdmin()
        return client.rows(TABLE, "select=*&order=criado_em.desc").map { row -> row.toPrayer() }
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toPrayer(): Prayer = Prayer(
        id = getString("id"),
        name = getString("nome"),
        text = getString("pedido"),
        date = getString("criado_em"),
    )
}
