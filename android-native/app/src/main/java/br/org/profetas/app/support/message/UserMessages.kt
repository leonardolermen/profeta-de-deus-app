package br.org.profetas.app.support.message

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Fila de mensagens curtas (snackbar) que qualquer ViewModel pode publicar. */
class UserMessages {

    // show() é chamado de código não suspenso; com buffer cheio descarta a mais antiga em vez de travar quem chama.
    private val outgoing = MutableSharedFlow<String>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val messages: SharedFlow<String> = outgoing.asSharedFlow()

    fun show(text: String) {
        outgoing.tryEmit(text)
    }
}
