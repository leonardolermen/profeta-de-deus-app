package br.org.profetas.app.support.storage

import br.org.profetas.app.support.http.encodeUrlComponent

/** Buckets públicos do Supabase Storage; o banco guarda só o caminho dentro do bucket, nunca a URL. */
enum class StorageBucket(val id: String) {
    EVENTS("eventos"),
    GALLERY("galeria");

    fun publicUrl(baseUrl: String, path: String): String {
        val encodedPath = path.split('/').joinToString("/") { segment -> encodeUrlComponent(segment) }

        return baseUrl.trimEnd('/') + "/storage/v1/object/public/$id/$encodedPath"
    }
}
