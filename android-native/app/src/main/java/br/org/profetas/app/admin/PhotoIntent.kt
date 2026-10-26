package br.org.profetas.app.admin

import android.net.Uri

// O que o admin pediu para a foto ao salvar. Tipo em vez de um "removePhoto: Boolean" ao lado de um Uri opcional:
// assim "trocar e remover ao mesmo tempo" nem chega a existir.
sealed interface PhotoIntent {

    data object Keep : PhotoIntent

    data class Replace(val source: Uri) : PhotoIntent

    data object Remove : PhotoIntent
}
