package br.org.profetas.app.account

/** Barreira do app antes de toda escrita de conteúdo da igreja. */
class ContentWriteGuard(private val account: AccountRepository) {

    suspend fun requireAdmin() {
        // Reconsulta o cargo: nao aceita permissao antiga se o acesso foi revogado.
        // A RLS do banco continua sendo a autoridade, inclusive para chamadas externas ao app.
        ContentAccess.requireAdmin(account.profile())
    }
}
