package br.org.profetas.app.support.http

import org.json.JSONArray
import org.json.JSONObject

suspend fun SupabaseClient.rows(table: String, query: String = "select=*"): List<JSONObject> {
    val array = JSONArray(get("/rest/v1/$table?$query"))

    return (0 until array.length()).map { index -> array.getJSONObject(index) }
}

// Sem id cria, com id atualiza: o mesmo formulário do admin serve para novo cadastro e para edição.
suspend fun SupabaseClient.saveRow(table: String, id: String?, body: JSONObject) {
    if (id == null) {
        post("/rest/v1/$table", body)
    } else {
        patch("/rest/v1/$table?id=eq.$id", body)
    }
}

suspend fun SupabaseClient.deleteRow(table: String, id: String) {
    delete("/rest/v1/$table?id=eq.$id")
}

fun JSONObject.textOrEmpty(key: String): String = if (isNull(key)) "" else optString(key)

fun JSONObject.textOrNull(key: String): String? = if (isNull(key)) null else getString(key)
