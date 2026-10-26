package br.org.profetas.app.church

import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.rows
import org.json.JSONObject

private const val TABLE = "igreja"

// A tabela igreja tem uma linha só, com id fixo 1 (check no banco).
private const val SINGLE_ROW_ID = "1"

class ChurchRepository(private val client: SupabaseClient, private val guard: ContentWriteGuard) {

    suspend fun details(): Church = client.rows(TABLE).firstOrNull()?.toChurch() ?: Church()

    suspend fun save(church: Church) {
        guard.requireAdmin()

        client.patch("/rest/v1/$TABLE?id=eq.$SINGLE_ROW_ID", church.toRow())
    }

    private fun JSONObject.toChurch(): Church = Church(
        name = getString("nome"),
        phone = optString("telefone"),
        whatsapp = optString("whatsapp"),
        email = optString("email"),
        address = optString("endereco"),
        history = optString("historia"),
        privacy = optString("privacidade"),
    )

    private fun Church.toRow(): JSONObject = JSONObject()
        .put("nome", name)
        .put("telefone", phone)
        .put("whatsapp", whatsapp)
        .put("email", email)
        .put("endereco", address)
        .put("historia", history)
        .put("privacidade", privacy)
}
