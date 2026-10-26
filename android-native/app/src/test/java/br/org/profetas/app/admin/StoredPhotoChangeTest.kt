package br.org.profetas.app.admin

import android.net.Uri
import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.IOException

private class UnreadableImageStorage : PhotoStorage {

    override suspend fun upload(bucket: StorageBucket, source: Uri): String =
        throw IOException("Nao foi possivel ler a imagem")

    override suspend fun remove(bucket: StorageBucket, path: String) {
    }
}

class StoredPhotoChangeTest {

    @Test fun failedUploadKeepsRecordAndExplainsInPortuguese() = runTest {
        val writes = mutableListOf<String?>()
        val change = StoredPhotoChange(UnreadableImageStorage(), StorageBucket.EVENTS)

        val thrown = runCatching {
            change.save("old.jpg", PhotoIntent.Replace(mock(Uri::class.java))) { path -> writes += path }
        }.exceptionOrNull()

        assertTrue(thrown is PhotoUploadException)
        assertEquals("Não foi possível enviar a foto. Verifique a conexão ou escolha outra imagem.", thrown?.message)
        assertEquals(emptyList<String?>(), writes)
    }
}
