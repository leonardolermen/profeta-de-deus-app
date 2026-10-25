package br.org.profetas.app.admin

import android.net.Uri
import br.org.profetas.app.gallery.GalleryWriter
import br.org.profetas.app.gallery.Photo
import br.org.profetas.app.gallery.PhotoDraft
import br.org.profetas.app.gallery.PhotoSource
import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

private class RecordingGalleryStorage(private val calls: MutableList<String>) : PhotoStorage {

    override suspend fun upload(bucket: StorageBucket, source: Uri): String {
        calls += "upload:${bucket.id}"

        return "new.jpg"
    }

    override suspend fun remove(bucket: StorageBucket, path: String) {
        calls += "remove:$path"
    }
}

private class RecordingGallery(private val calls: MutableList<String>) : GalleryWriter {

    var saved: PhotoDraft? = null

    override suspend fun save(id: String?, draft: PhotoDraft) {
        calls += "save:$id"

        saved = draft
    }

    override suspend fun delete(id: String) {
        calls += "delete:$id"
    }
}

class GalleryPhotoChangeTest {

    private val picked: Uri = mock(Uri::class.java)

    private val legacy = Photo("7", "Batismo", "Batismos", url = "https://antigo.example/a.jpg", path = null)

    @Test fun replacingLegacyUrlPhotoStoresPathInGalleryBucket() = runTest {
        val calls = mutableListOf<String>()
        val gallery = RecordingGallery(calls)

        GalleryPhotoChange(RecordingGalleryStorage(calls), gallery)
            .save(legacy, "Batismo", "Batismos", PhotoIntent.Replace(picked))

        assertEquals(listOf("upload:galeria", "save:7"), calls)
        assertEquals(PhotoSource.StoragePath("new.jpg"), gallery.saved?.source)
    }

    @Test fun keepingLegacyPhotoKeepsItsUrl() = runTest {
        val calls = mutableListOf<String>()
        val gallery = RecordingGallery(calls)

        GalleryPhotoChange(RecordingGalleryStorage(calls), gallery)
            .save(legacy, "Batismo de junho", "Batismos", PhotoIntent.Keep)

        assertEquals(listOf("save:7"), calls)
        assertEquals(PhotoSource.ExternalUrl("https://antigo.example/a.jpg"), gallery.saved?.source)
    }

    @Test fun newPhotoWithoutPickedImageIsRejected() = runTest {
        val calls = mutableListOf<String>()

        val thrown = runCatching {
            GalleryPhotoChange(RecordingGalleryStorage(calls), RecordingGallery(calls))
                .save(null, "Batismo", "Batismos", PhotoIntent.Keep)
        }.exceptionOrNull()

        assertTrue(thrown is IllegalArgumentException)
        assertEquals("Escolha uma foto.", thrown?.message)
        assertEquals(emptyList<String>(), calls)
    }
}
