package br.org.profetas.app.ui

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.org.profetas.app.*

@Composable fun AuthScreen(s:AppState,vm:AppViewModel,done:()->Unit) = Page {
    var register by rememberSaveable {mutableStateOf(false)}
    var recover by rememberSaveable {mutableStateOf(false)}
    var name by rememberSaveable {mutableStateOf("")}
    var email by rememberSaveable {mutableStateOf("")}
    // Senhas nao entram no estado salvo da Activity.
    var password by remember {mutableStateOf("")}
    Heading(if(recover) "Recuperar acesso" else if(register) "Faça parte da comunidade" else "Bem-vindo de volta")
    if(recover) Text("Enviaremos um link para o e-mail informado. Abra o link neste aparelho para definir uma nova senha.")
    if(register && !recover) Field("Nome completo",name,{name=it.take(120)})
    Field("E-mail",email,{email=it.take(254)})
    if(!recover) OutlinedTextField(password,{password=it},label={Text("Senha")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
    if(register && !recover) Text("Use pelo menos 8 caracteres. Consulte a política de privacidade nos ajustes.",style=MaterialTheme.typography.bodySmall)
    val valid=Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() && (recover || (password.length >= (if(register) 8 else 1) && (!register || name.trim().length>=2)))
    Button(enabled=valid && !s.saving,onClick={
        if(recover) vm.action("Se houver uma conta com esse e-mail, você receberá as instruções de recuperação.") {resetPassword(email)}
        else if(register) vm.register(name,email,password,done)
        else vm.login(email,password,done)
    }) {Text(if(recover) "Enviar instruções" else if(register) "Criar conta" else "Entrar")}
    TextButton(onClick={register=!register;recover=false;password=""}) {Text(if(register) "Já tenho uma conta" else "Criar conta")}
    TextButton(onClick={recover=!recover;password=""}) {Text(if(recover) "Voltar ao login" else "Esqueci minha senha")}
}
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
@Composable fun ProfileScreen(s:AppState,vm:AppViewModel) = Page {
    if(!s.loggedIn) {Empty("Entre na sua conta para editar o perfil."); return@Page}
    Heading("Meu perfil",s.email)
    var name by rememberSaveable(s.profile?.id,s.profile?.name) {mutableStateOf(s.profile?.name.orEmpty())}
    Field("Nome completo",name,{name=it.take(120)})
    Button(enabled=!s.saving && name.trim().length>=2,onClick={vm.action("Perfil atualizado.") {saveProfile(name)}}) {Text("Salvar perfil")}
    HorizontalDivider()
    var password by remember {mutableStateOf("")}
    var confirm by remember {mutableStateOf("")}
    Heading("Alterar senha")
    OutlinedTextField(password,{password=it},label={Text("Nova senha")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(confirm,{confirm=it},label={Text("Repita a nova senha")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
    Button(enabled=!s.saving && password.length>=8 && password==confirm,onClick={vm.action("Senha atualizada.",{password="";confirm=""}) {updatePassword(password)}}) {Text("Atualizar senha")}
}
