package br.org.profetas.app.admin

import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventDraft
import br.org.profetas.app.event.EventWriter
import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket

// A foto atual do evento é a do rascunho (draft.imagePath): o editor parte do evento recarregado depois de cada
// gravação, então não troca a foto de uma versão velha da lista.
class EventPhotoChange(storage: PhotoStorage, private val events: EventWriter) {

    private val change = StoredPhotoChange(storage, StorageBucket.EVENTS)

    suspend fun save(id: String?, draft: EventDraft, photo: PhotoIntent) {
        change.save(draft.imagePath, photo) { path ->
            events.save(id, draft.copy(imagePath = path))
        }
    }

    suspend fun delete(event: Event) {
        events.delete(event.id)

        change.discard(event.imagePath)
    }
}
