package br.org.profetas.app.support.storage

import br.org.profetas.app.support.http.encodeUrlComponent

/** Buckets privados; as imagens usam a sessão do usuário e políticas do Storage. */
enum class StorageBucket(val id: String) {
    EVENTS("eventos"),
    GALLERY("galeria");

    fun authenticatedUrl(baseUrl: String, path: String): String {
        val encodedPath = path.split('/').joinToString("/") { segment -> encodeUrlComponent(segment) }

        return baseUrl.trimEnd('/') + "/storage/v1/object/authenticated/$id/$encodedPath"
    }
}
