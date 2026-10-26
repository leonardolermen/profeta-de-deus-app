package br.org.profetas.app.admin

import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.account.Profile

// O que a administração mostra antes do conteúdo. Logado e sem perfil é carga, não recusa: o perfil só existe depois
// de carregado. Mas se a carga falhou, girar para sempre deixava o admin sem saída; a falha vira um estado próprio,
// com "Tentar novamente".
sealed interface AdminAccess {

    data object Granted : AdminAccess

    data object Loading : AdminAccess

    data class Failed(val message: String) : AdminAccess

    data object Denied : AdminAccess

    companion object {

        fun of(profile: Profile?, accountId: String?, profileFailure: String?): AdminAccess = when {
            ContentAccess.canManage(profile) -> Granted
            accountId == null || profile != null -> Denied
            profileFailure != null -> Failed(profileFailure)
            else -> Loading
        }
    }
}
