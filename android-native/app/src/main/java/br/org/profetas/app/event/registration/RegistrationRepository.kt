package br.org.profetas.app.event.registration

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.deleteRow
import br.org.profetas.app.support.http.rows
import org.json.JSONObject

private const val TABLE = "inscricoes"

class RegistrationRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun mine(): List<Registration> {
        val userId = client.session?.userId ?: return emptyList()

        return client.rows(TABLE, "select=*&usuario_id=eq.$userId&order=criado_em.desc").map { row ->
            row.toRegistration()
        }
    }

    // A RLS decide o que volta: o membro vê só as próprias inscrições, o admin vê todas.
    suspend fun all(): List<Registration> =
        client.rows(TABLE, "select=*&order=criado_em.desc").map { row -> row.toRegistration() }

    suspend fun enroll(eventId: String, name: String, phone: String) {
        val body = JSONObject()
            .put("evento_id", eventId)
            .put("usuario_id", requireNotNull(client.session).userId)
            .put("nome", name.trim())
            .put("telefone", phone.trim())

        client.post("/rest/v1/$TABLE", body)
    }

    suspend fun cancel(id: String) {
        client.deleteRow(TABLE, id)
    }

    suspend fun deleteAsAdmin(id: String) {
        guard.requireAdmin()

        client.deleteRow(TABLE, id)
    }

    private fun JSONObject.toRegistration(): Registration = Registration(
        id = getString("id"),
        eventId = getString("evento_id"),
        name = getString("nome"),
        phone = getString("telefone"),
        userId = getString("usuario_id"),
    )
}
