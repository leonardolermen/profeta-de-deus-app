package br.org.profetas.app.support.storage

import android.net.Uri
import br.org.profetas.app.support.http.SupabaseClient
import org.json.JSONArray
import org.json.JSONObject
import java.time.Year
import java.util.UUID

private const val JPEG_QUALITY = 80
private const val JPEG_CONTENT_TYPE = "image/jpeg"

interface ImageReader {

    suspend fun readJpeg(source: Uri, maxSide: Int, quality: Int): ByteArray
}

class ImageUploader(private val client: SupabaseClient, private val reader: ImageReader) : PhotoStorage {

    /** Devolve o caminho dentro do bucket (`yyyy/<uuid>.jpg`), que é o que o banco guarda. */
    override suspend fun upload(bucket: StorageBucket, source: Uri): String {
        val bytes = reader.readJpeg(source, DEFAULT_MAX_SIDE, JPEG_QUALITY)

        // Nome aleatório: duas pessoas enviando "IMG_001" nunca se sobrescrevem; o ano só organiza a pasta.
        val path = "${Year.now().value}/${UUID.randomUUID()}.jpg"
        client.upload("/storage/v1/object/${bucket.id}/$path", bytes, JPEG_CONTENT_TYPE)

        return path
    }

    override suspend fun remove(bucket: StorageBucket, path: String) {
        val body = JSONObject().put("prefixes", JSONArray().put(path))

        client.deleteWithBody("/storage/v1/object/${bucket.id}", body)
    }
}
