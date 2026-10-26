package br.org.profetas.app.admin

import android.net.Uri
import br.org.profetas.app.event.EventDraft
import br.org.profetas.app.event.EventType
import br.org.profetas.app.event.EventWriter
import br.org.profetas.app.support.http.ApiException
import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.IOException

private class RecordingStorage(private val calls: MutableList<String>) : PhotoStorage {

    override suspend fun upload(bucket: StorageBucket, source: Uri): String {
        calls += "upload"

        return "new.jpg"
    }

    override suspend fun remove(bucket: StorageBucket, path: String) {
        calls += "remove:$path"
    }
}

private class RecordingEvents(private val calls: MutableList<String>, private val failure: Exception? = null) :
    EventWriter {

    var saved: EventDraft? = null

    override suspend fun save(id: String?, draft: EventDraft) {
        calls += "save"
        failure?.let { exception -> throw exception }

        saved = draft
    }

    override suspend fun delete(id: String) {
        calls += "delete"
    }
}

class EventPhotoChangeTest {

    private val draft = EventDraft(
        title = "Culto de Santa Ceia",
        description = "",
        date = "2026-11-07T19:30-03:00",
        type = EventType.CULTO,
        imagePath = "old.jpg",
    )

    private val picked: Uri = mock(Uri::class.java)

    @Test fun oldPhotoRemovedOnlyAfterSave() = runTest {
        val calls = mutableListOf<String>()
        val events = RecordingEvents(calls)

        EventPhotoChange(RecordingStorage(calls), events).save("1", draft, PhotoIntent.Replace(picked))

        assertEquals(listOf("upload", "save", "remove:old.jpg"), calls)
        assertEquals("new.jpg", events.saved?.imagePath)
    }

    @Test fun failedSaveRemovesNewUploadAndKeepsOld() = runTest {
        val calls = mutableListOf<String>()
        val events = RecordingEvents(calls, failure = ApiException(400, "Falha na solicitação."))
        val change = EventPhotoChange(RecordingStorage(calls), events)

        val thrown = runCatching { change.save("1", draft, PhotoIntent.Replace(picked)) }.exceptionOrNull()

        assertTrue(thrown is ApiException)
        assertEquals(listOf("upload", "save", "remove:new.jpg"), calls)
    }

    // Sem resposta, o PATCH pode ter chegado ao banco: apagar o arquivo novo deixaria o evento apontando para nada.
    @Test fun failedSaveWithUnknownOutcomeKeepsNewUpload() = runTest {
        val calls = mutableListOf<String>()
        val events = RecordingEvents(calls, failure = IOException("timeout"))
        val change = EventPhotoChange(RecordingStorage(calls), events)

        val thrown = runCatching { change.save("1", draft, PhotoIntent.Replace(picked)) }.exceptionOrNull()

        assertTrue(thrown is IOException)
        assertEquals("timeout", thrown?.message)
        assertEquals(listOf("upload", "save"), calls)
    }

    @Test fun removePhotoClearsPathThenDeletes() = runTest {
        val calls = mutableListOf<String>()
        val events = RecordingEvents(calls)

        EventPhotoChange(RecordingStorage(calls), events).save("1", draft, PhotoIntent.Remove)

        assertEquals(listOf("save", "remove:old.jpg"), calls)
        assertNull(events.saved?.imagePath)
    }
}
