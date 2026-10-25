package br.org.profetas.app.gallery

// A parte do GalleryRepository que a troca de foto do admin usa. Interface para que os testes registrem a ordem
// entre gravar a foto e mexer no Storage com um falso, sem rede.
interface GalleryWriter {

    suspend fun save(id: String?, draft: PhotoDraft)

    suspend fun delete(id: String)
}
