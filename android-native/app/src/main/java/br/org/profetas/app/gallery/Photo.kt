package br.org.profetas.app.gallery

import br.org.profetas.app.support.storage.StorageBucket

/** Foto nova guarda o caminho no bucket galeria; foto antiga guarda uma URL HTTPS externa. */
data class Photo(
    val id: String,
    val title: String,
    val album: String,
    val url: String?,
    val path: String?,
) {

    fun imageUrl(baseUrl: String): String? =
        path?.let { storedPath -> StorageBucket.GALLERY.publicUrl(baseUrl, storedPath) } ?: url
}

data class PhotoDraft(
    val title: String,
    val url: String,
    val album: String,
)
