package br.org.profetas.app.birthday

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import org.json.JSONObject

private const val TABLE = "aniversariantes"

class BirthdayRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun all(): List<Birthday> =
        client.rows(TABLE, "select=*&order=dia.asc").map { row -> row.toBirthday() }

    suspend fun save(id: String?, draft: BirthdayDraft) {
        guard.requireAdmin()

        client.saveRow(TABLE, id, draft.toRow())
    }

    suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toBirthday(): Birthday = Birthday(
        id = getString("id"),
        name = getString("nome"),
        day = getInt("dia"),
        month = getInt("mes"),
        consent = getBoolean("consentimento"),
    )

    private fun BirthdayDraft.toRow(): JSONObject = JSONObject()
        .put("nome", name)
        .put("dia", day)
        .put("mes", month)
        .put("consentimento", consent)
}
