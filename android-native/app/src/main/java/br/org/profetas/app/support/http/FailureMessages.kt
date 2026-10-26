package br.org.profetas.app.support.http

import org.json.JSONObject
import java.io.IOException

// Sem rede, o OkHttp lança IOException com texto técnico em inglês ("Unable to resolve host…"). Não é ApiException:
// quem trata ApiException conta com uma resposta do servidor, e aqui a requisição pode nem ter chegado lá.
class ConnectionException(cause: IOException) :
    IOException("Sem conexão com a internet. Verifique a rede e tente novamente.", cause)

// O texto do servidor vem em inglês e pode expor detalhes internos, por isso nunca chega ao usuário: só os códigos
// do GoTrue que o app sabe explicar ganham mensagem própria; o resto cai na mensagem por status ou na genérica.
private val AUTH_CODE_MESSAGES = mapOf(
    "invalid_credentials" to "E-mail ou senha incorretos.",
    "user_already_exists" to "Já existe uma conta com este e-mail.",
    "weak_password" to "Senha fraca. Escolha uma senha mais forte.",
    "email_not_confirmed" to "Confirme seu e-mail antes de entrar.",
    "same_password" to "A nova senha precisa ser diferente da atual.",
    "over_email_send_rate_limit" to "Muitos e-mails enviados. Aguarde alguns minutos e tente novamente.",
)

private val STATUS_MESSAGES = mapOf(
    401 to "Sua sessão expirou. Entre novamente.",
    403 to "Você não tem permissão para esta ação.",
    409 to "Este cadastro já existe.",
    429 to "Muitas tentativas. Aguarde e tente novamente.",
)

internal object FailureMessages {

    fun forSupabase(status: Int, body: String): String =
        AUTH_CODE_MESSAGES[authErrorCode(body)] ?: STATUS_MESSAGES[status] ?: "Falha na solicitação ($status)."

    fun forExternal(status: Int): String =
        STATUS_MESSAGES[status] ?: "Não foi possível consultar a Bíblia ($status)."

    // O GoTrue manda o código em "error_code"; algumas respostas o trazem em "code" (em outras, "code" é o status
    // numérico, que simplesmente não está na tabela).
    private fun authErrorCode(body: String): String? {
        val error = runCatching { JSONObject(body) }.getOrNull() ?: return null

        return error.optString("error_code").ifBlank { error.optString("code") }
    }
}
