package br.org.profetas.app.admin

import br.org.profetas.app.gallery.GalleryWriter
import br.org.profetas.app.gallery.Photo
import br.org.profetas.app.gallery.PhotoDraft
import br.org.profetas.app.gallery.PhotoSource
import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket

// Foto antiga com URL externa mantém a URL até o admin escolher uma imagem nova; daí em diante passa a ter só o
// caminho no bucket. Não existe "remover foto" na galeria: a linha sem imagem não faria sentido (e o banco recusa).
class GalleryPhotoChange(storage: PhotoStorage, private val gallery: GalleryWriter) {

    private val change = StoredPhotoChange(storage, StorageBucket.GALLERY)

    suspend fun save(current: Photo?, title: String, album: String, photo: PhotoIntent) {
        val legacyUrl = current?.url.takeIf { current?.path == null }
        val hasImage = photo is PhotoIntent.Replace || current?.path != null || legacyUrl != null
        require(photo != PhotoIntent.Remove && hasImage) { "Escolha uma foto." }

        change.save(current?.path, photo) { path ->
            val source = path?.let(PhotoSource::StoragePath) ?: PhotoSource.ExternalUrl(requireNotNull(legacyUrl))

            gallery.save(current?.id, PhotoDraft(title = title, album = album, source = source))
        }
    }

    suspend fun delete(photo: Photo) {
        gallery.delete(photo.id)

        change.discard(photo.path)
    }
}
