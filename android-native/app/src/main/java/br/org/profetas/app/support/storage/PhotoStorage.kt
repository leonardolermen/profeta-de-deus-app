package br.org.profetas.app.support.storage

import android.net.Uri

// O que a troca de foto do admin usa do Storage. Interface para que os testes registrem a ordem das chamadas com
// um falso, sem rede nem um ImageReader de verdade.
interface PhotoStorage {

    suspend fun upload(bucket: StorageBucket, source: Uri): String

    suspend fun remove(bucket: StorageBucket, path: String)
}
