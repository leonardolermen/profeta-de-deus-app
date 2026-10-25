package br.org.profetas.app.event

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import br.org.profetas.app.support.http.saveRow
import br.org.profetas.app.support.http.textOrEmpty
import br.org.profetas.app.support.http.textOrNull
import org.json.JSONObject

private const val TABLE = "eventos"

class EventRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) : EventWriter {

    suspend fun all(): List<Event> =
        client.rows(TABLE, "select=*&order=data_evento.asc").map { row -> row.toEvent() }

    override suspend fun save(id: String?, draft: EventDraft) {
        guard.requireAdmin()

        client.saveRow(TABLE, id, draft.toRow())
    }

    override suspend fun delete(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toEvent(): Event = Event(
        id = getString("id"),
        title = getString("titulo"),
        description = textOrEmpty("descricao"),
        date = getString("data_evento"),
        type = EventType.fromDatabase(getString("tipo")),
        imagePath = textOrNull("imagem"),
    )

    // JSONObject.put com null apaga a chave; só o NULL explícito limpa a coluna imagem num PATCH.
    private fun EventDraft.toRow(): JSONObject = JSONObject()
        .put("titulo", title)
        .put("descricao", description)
        .put("data_evento", date)
        .put("tipo", type.databaseValue)
        .put("imagem", imagePath ?: JSONObject.NULL)
}
