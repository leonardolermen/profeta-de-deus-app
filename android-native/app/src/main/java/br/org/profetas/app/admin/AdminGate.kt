package br.org.profetas.app.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure

// Decide pelo perfil coletado como estado: ele é derivado da sessão em segundo plano, e recarregá-lo não o apaga
// (account.profile() só o troca ao terminar). Assim o puxar-para-atualizar não pisca "Acesso restrito" como no
// editor antigo, que lia um perfil zerado durante a recarga.
@Composable
fun AdminGate(access: AdminAccess, onRetry: () -> Unit, content: @Composable () -> Unit) {
    when (access) {
        AdminAccess.Granted -> content()
        AdminAccess.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        is AdminAccess.Failed -> Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            LoadFailure(message = access.message, onRetry = onRetry)
        }
        AdminAccess.Denied -> EmptyState("Acesso restrito à administração.")
    }
}
