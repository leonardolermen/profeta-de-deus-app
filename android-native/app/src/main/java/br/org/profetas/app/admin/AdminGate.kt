package br.org.profetas.app.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.org.profetas.app.account.ContentAccess
import br.org.profetas.app.account.Profile
import br.org.profetas.app.support.ui.EmptyState

// Decide pelo perfil coletado como estado: ele é derivado da sessão em segundo plano, e recarregá-lo não o apaga
// (account.profile() só o troca ao terminar). Assim o puxar-para-atualizar não pisca "Acesso restrito" como no
// editor antigo, que lia um perfil zerado durante a recarga. Logado e ainda sem perfil é carga, não recusa.
@Composable
fun AdminGate(profile: Profile?, isLoggedIn: Boolean, content: @Composable () -> Unit) {
    if (ContentAccess.canManage(profile)) {
        content()
    } else if (isLoggedIn && profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        EmptyState("Acesso restrito à administração.")
    }
}
