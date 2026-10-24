package br.org.profetas.app.support.storage

import java.net.URLEncoder

/** Buckets públicos do Supabase Storage; o banco guarda só o caminho dentro do bucket, nunca a URL. */
enum class StorageBucket(val id: String) {
    EVENTS("eventos"),
    GALLERY("galeria");

    fun publicUrl(baseUrl: String, path: String): String {
        val encodedPath = path.split('/').joinToString("/") { segment -> encodeSegment(segment) }

        return baseUrl.trimEnd('/') + "/storage/v1/object/public/$id/$encodedPath"
    }

    // URLEncoder segue o formato de formulário (espaço vira "+"), mas num caminho de URL o "+" é literal.
    private fun encodeSegment(segment: String): String =
        URLEncoder.encode(segment, Charsets.UTF_8.name()).replace("+", "%20")
}
