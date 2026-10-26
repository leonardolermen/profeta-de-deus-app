package br.org.profetas.app.event.weekly

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import org.json.JSONArray
import org.json.JSONObject

private const val TABLE = "cultos_semanais"

class WeeklyServiceRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun all(): List<WeeklyService> =
        client.rows(TABLE, "select=*&order=ordem.asc,dia_semana.asc").map(WeeklyService::fromRow)

    suspend fun save(id: String?, draft: WeeklyServiceDraft) {
        guard.requireAdmin()

        client.saveRow(TABLE, id, draft.toRow())
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun WeeklyServiceDraft.toRow(): JSONObject = JSONObject()
        .put("nome", name)
        .put("dia_semana", dayOfWeek.databaseDay)
        .put("horarios", JSONArray(times))
        .put("destaque", highlight)
        .put("local", place)
        .put("ordem", order)
}
