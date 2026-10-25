package br.org.profetas.app.admin

import br.org.profetas.app.support.storage.PhotoStorage
import br.org.profetas.app.support.storage.StorageBucket
import kotlinx.coroutines.CancellationException
import java.io.IOException

// Ler a imagem escolhida e enviá-la falham do mesmo jeito para o admin (IOException com texto técnico, em inglês ou
// sem acento): uma mensagem só, em português, que diz o que fazer.
class PhotoUploadException(cause: IOException) :
    IOException("Não foi possível enviar a foto. Verifique a conexão ou escolha outra imagem.", cause)

// A regra de ordem da troca de foto, comum a eventos (bucket eventos) e à galeria (bucket galeria).
class StoredPhotoChange(private val storage: PhotoStorage, private val bucket: StorageBucket) {

    // Ordem obrigatória: envia a foto nova, grava o registro apontando para ela e só então apaga a antiga. Se o
    // envio ou a gravação falham no meio, o registro continua apontando para a foto antiga, que ainda existe; o
    // arquivo novo, que nenhum registro referencia, é apagado para não ficar órfão no bucket. Apagar a antiga antes
    // de gravar deixaria o evento apontando para um arquivo que não existe mais.
    suspend fun save(currentPath: String?, photo: PhotoIntent, write: suspend (path: String?) -> Unit) {
        when (photo) {
            PhotoIntent.Keep -> write(currentPath)
            PhotoIntent.Remove -> {
                write(null)
                discard(currentPath)
            }
            is PhotoIntent.Replace -> {
                val newPath = upload(photo)

                writeOrDiscard(newPath, write)
                discard(currentPath)
            }
        }
    }

    // Depois de o registro já não apontar para o arquivo (excluído ou trocado).
    suspend fun discard(path: String?) {
        if (path == null) {
            return
        }

        try {
            storage.remove(bucket, path)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }

            // O registro já foi gravado sem esse arquivo: a falha só deixa um arquivo sem uso no bucket. Avisar
            // erro faria o admin achar que a troca não aconteceu e repeti-la.
        }
    }

    private suspend fun upload(photo: PhotoIntent.Replace): String {
        try {
            return storage.upload(bucket, photo.source)
        } catch (e: IOException) {
            throw PhotoUploadException(e)
        }
    }

    private suspend fun writeOrDiscard(newPath: String, write: suspend (path: String?) -> Unit) {
        try {
            write(newPath)
        } catch (e: Exception) {
            discard(newPath)

            throw e
        }
    }
}
