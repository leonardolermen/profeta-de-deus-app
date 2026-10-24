package br.org.profetas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.org.profetas.app.*

@Composable fun SettingsScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Ajustes",if(s.loggedIn) s.profile?.name ?: s.email else "Você está navegando como visitante.")
    LoadStatus(s,"Conta",vm::reload)
    Panel(
        if(s.isAdmin) "Modo administrador" else if(s.loggedIn) "Modo usuário" else "Modo visitante",
        if(s.isAdmin) "Você pode gerenciar o conteúdo da igreja e acompanhar pedidos e inscrições."
        else "Consulte as informações da igreja. Ao entrar, você também pode enviar pedidos e gerenciar suas próprias inscrições."
    )
    if(s.loggedIn) {
        Panel("Meu perfil",s.email,{go("profile")})
        Panel("Minhas inscrições",onClick={go("registrations")})
        OutlinedButton(enabled=!s.saving,onClick=vm::logout) {Text("Sair da conta")}
    } else Button(onClick={go("auth")}) {Text("Entrar ou criar conta")}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {Text("Tema escuro",Modifier.padding(top=12.dp)); Switch(s.dark,vm::setDark)}
    Panel("Lembretes de eventos", "${s.reminders.size} lembrete(s) selecionado(s). Escolha um evento na agenda para ativar.") {
        TextButton(onClick=vm::cancelReminders,enabled=s.reminders.isNotEmpty()) {Text("Desativar todos")}
    }
    Panel("Privacidade",onClick={go("privacy")})
    Panel("Contatos da igreja",onClick={go("contact")})
    if(s.isAdmin) Panel("Administração", "Gerencie a agenda e os conteúdos da comunidade.",{go("admin")})
    Text("Profetas de Deus • Android • 2.0.0",style=MaterialTheme.typography.bodySmall)
}
