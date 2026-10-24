package br.org.profetas.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.org.profetas.app.*
import br.org.profetas.app.R
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventType
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.event.isFuture
import br.org.profetas.app.support.intent.openLink
import br.org.profetas.app.support.intent.share
import coil.compose.SubcomposeAsyncImage
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable fun HomeScreen(s: AppState, configured: Boolean, go: (String)->Unit) = Page {
    Image(painterResource(R.drawable.logo),contentDescription="Igreja Profetas de Deus",modifier=Modifier.fillMaxWidth().height(110.dp),contentScale=ContentScale.Fit)
    Heading("Uma comunidade de fé", "Que bom ter você aqui${s.profile?.name?.let { ", $it" } ?: ""}.")
    if(!configured) Panel("Sua comunidade estará aqui", "As informações estarão disponíveis após a conexão dos serviços da igreja.")
    Panel("Juntos em oração", "Compartilhe seu pedido com a equipe pastoral.", { go("prayer") })
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick={go("bible")}) { Text("Ler a Bíblia") }
        OutlinedButton(onClick={go("studies")}) { Text("Estudos") }
    }
    Heading("Próximos encontros")
    s.errors["Eventos"]?.let { Empty(it) }
    val events = s.events.filter { isFuture(it.date) }.take(3)
    if(events.isEmpty() && !s.loading) Empty("Nenhum encontro publicado para os próximos dias.")
    events.forEach { EventCard(it) { go("event/${it.id}") } }
    TextButton(onClick={go("events")}) { Text("Ver agenda completa") }
    Panel("Conheça nossa história", "Nossa missão e caminhada.", {go("history")})
    Panel("Fale com a igreja", "Contatos e localização.", {go("contact")})
}
@Composable fun CommunityScreen(go:(String)->Unit) = Page {
    Heading("Comunidade", "Caminhamos melhor juntos.")
    listOf("Pedidos de oração" to "prayer", "Estudos e trilhas" to "studies", "Aniversariantes" to "birthdays", "Galeria de fotos" to "gallery", "Minhas inscrições" to "registrations", "Nossa história" to "history", "Contatos e localização" to "contact").forEach { (title,path) -> Panel(title,onClick={go(path)}) }
}
@Composable fun EventCard(event: Event, click:()->Unit) = Panel(event.title, "${displayDate(event.date)} • ${event.type.label}", click)
@Composable fun EventsScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Agenda da comunidade")
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("todos") }
    var past by rememberSaveable { mutableStateOf(false) }
    Field("Buscar evento",query,{query=it})
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("todos","culto","campanha","batismo").forEach { item -> FilterChip(selected=type==item,onClick={type=item},label={Text(item.replaceFirstChar { it.uppercase() })}) }
    }
    Row { Checkbox(past,{past=it}); Text("Incluir eventos anteriores",Modifier.padding(top=12.dp)) }
    LoadStatus(s,"Eventos",vm::reload)
    val filtered=s.events.filter { (past || isFuture(it.date)) && (type=="todos" || type==it.type.databaseValue) && "${it.title} ${it.description}".contains(query,true) }
    if(filtered.isEmpty() && !s.loading) Empty("Nenhum evento encontrado.")
    filtered.forEach { EventCard(it) {go("event/${it.id}")} }
}
@Composable fun EventScreen(s:AppState,vm:AppViewModel,id:String,go:(String)->Unit) = Page {
    val event=s.events.find { it.id==id }
    if(event==null) { Empty(if(s.loading) "Carregando evento…" else "Evento indisponível."); LoadStatus(s,"Eventos",vm::reload); return@Page }
    val context=LocalContext.current
    Heading(event.title,displayDate(event.date))
    Text(event.description.ifBlank {"Mais informações serão publicadas pela igreja."})
    OutlinedButton(onClick={share(context,"${event.title}\n${displayDate(event.date)}\n${event.description}")}) {Text("Compartilhar evento")}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed -> if(allowed) vm.reminder(event,true) else vm.message("Permita as notificações nas configurações do Android para receber o lembrete.") }
    if(isFuture(event.date)) {
        OutlinedButton(onClick={
            if(id in s.reminders) vm.reminder(event,false)
            else if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else vm.reminder(event,true)
        }) {Text(if(id in s.reminders) "Remover lembrete" else "Lembrar 1 hora antes")}
        Text("O horário do lembrete depende das condições de bateria e conexão do aparelho.",style=MaterialTheme.typography.bodySmall)
        if(!s.loggedIn) Button(onClick={go("auth")}) {Text("Entrar para se inscrever")}
        else {
            val registration=s.registrations.find {it.eventId==id && it.userId==s.profile?.id}
            if(registration!=null) Panel("Inscrição confirmada",registration.name) { TextButton(onClick={go("registrations")}) {Text("Gerenciar inscrição")} }
            else {
                var name by rememberSaveable(s.profile?.id) {mutableStateOf(s.profile?.name.orEmpty())}
                var phone by rememberSaveable {mutableStateOf("")}
                var accepted by rememberSaveable {mutableStateOf(false)}
                Heading(if(event.type==EventType.BATISMO) "Quero me batizar" else "Participar deste evento")
                Field("Nome completo",name,{name=it.take(120)})
                Field("Telefone com DDD",phone,{phone=it.take(25)})
                Row {Checkbox(accepted,{accepted=it}); Text("Autorizo o contato da igreja sobre esta inscrição.",Modifier.weight(1f).padding(top=10.dp))}
                TextButton(onClick={go("privacy")}) {Text("Consultar privacidade")}
                Button(enabled=!s.saving && name.trim().length>=2 && phone.count {it.isDigit()}>=8 && accepted,onClick={vm.action("Inscrição confirmada!") {registrations.enroll(id,name,phone)}}) {Text("Confirmar inscrição")}
            }
        }
    } else Empty("Este evento já aconteceu.")
}
@Composable fun PrayerScreen(s:AppState,vm:AppViewModel,done:()->Unit) = Page {
    Heading("Como podemos orar por você?", "Seu pedido será acessado somente pela administração pastoral.")
    var name by rememberSaveable {mutableStateOf("")}
    var text by rememberSaveable {mutableStateOf("")}
    Field("Seu nome",name,{name=it.take(120)})
    Field("Pedido de oração",text,{text=it.take(4000)},true)
    Text("${text.length}/4000",style=MaterialTheme.typography.bodySmall)
    Button(enabled=!s.saving && name.trim().length>=2 && text.trim().length>=5,onClick={vm.action("Pedido enviado à equipe pastoral.",done) {prayers.send(name,text)}}) {Text("Enviar pedido")}
}
@Composable fun StudiesScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Estudos e trilhas", "Leia no seu ritmo e acompanhe suas conclusões neste aparelho.")
    var query by rememberSaveable {mutableStateOf("")}
    Field("Buscar estudo ou tema",query,{query=it})
    LoadStatus(s,"Estudos",vm::reload)
    val studies=s.studies.filter {"${it.title} ${it.category} ${it.description}".contains(query,true)}
    if(studies.isEmpty() && !s.loading) Empty("Nenhum estudo encontrado.")
    studies.forEach { study -> Panel(study.title,"${study.category}${if(study.id in s.completed) " • Concluído" else ""}\n${study.description}",{go("study/${study.id}")}) }
}
@Composable fun StudyScreen(s:AppState,vm:AppViewModel,id:String) = Page {
    val study=s.studies.find {it.id==id}
    if(study==null) {Empty("Estudo indisponível."); return@Page}
    val context=LocalContext.current
    Heading(study.title,study.category)
    Text(study.description)
    HorizontalDivider()
    Text(study.content,style=MaterialTheme.typography.bodyLarge)
    Button(onClick={vm.toggleStudy(id)}) {Text(if(id in s.completed) "Marcar como não concluído" else "Concluir estudo")}
    OutlinedButton(onClick={share(context,"${study.title}\n\n${study.content}")}) {Text("Compartilhar estudo")}
}
@Composable fun BirthdaysScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Aniversariantes", "Celebre a vida de quem caminha com você.")
    if(!s.loggedIn) {Empty("Entre para consultar os aniversariantes que autorizaram a divulgação."); Button(onClick={go("auth")}) {Text("Entrar")}; return@Page}
    var month by rememberSaveable {mutableIntStateOf(LocalDate.now().monthValue)}
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        (1..12).forEach { m -> FilterChip(selected=month==m,onClick={month=m},label={Text(Month.of(m).getDisplayName(TextStyle.SHORT,Locale.forLanguageTag("pt-BR")))}) }
    }
    LoadStatus(s,"Aniversariantes",vm::reload)
    val birthdays=s.birthdays.filter {it.month==month}
    if(birthdays.isEmpty() && !s.loading) Empty("Nenhum aniversariante publicado neste mês.")
    birthdays.forEach {Panel(it.name,"Dia ${it.day}")}
}
@Composable fun GalleryScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Memórias da comunidade")
    var album by rememberSaveable {mutableStateOf("Todos")}
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        (listOf("Todos")+s.photos.map {it.album}.filter {it.isNotBlank()}.distinct()).forEach { a -> FilterChip(selected=album==a,onClick={album=a},label={Text(a)}) }
    }
    LoadStatus(s,"Galeria",vm::reload)
    val photos=s.photos.filter {album=="Todos" || it.album==album}
    if(photos.isEmpty() && !s.loading) Empty("Nenhuma foto publicada neste álbum.")
    photos.forEach { photo -> Panel(photo.title,photo.album,{go("photo/${photo.id}")}) {
        SubcomposeAsyncImage(model=photo.imageUrl(vm.storageBaseUrl),contentDescription=photo.title,modifier=Modifier.fillMaxWidth().height(200.dp),contentScale=ContentScale.Crop,loading={CircularProgressIndicator()},error={Empty("Imagem indisponível.")})
    } }
}
@Composable fun PhotoScreen(s:AppState,storageBaseUrl:String,id:String) = Page {
    val photo=s.photos.find {it.id==id}
    if(photo==null) {Empty("Foto indisponível."); return@Page}
    val context=LocalContext.current
    val imageUrl=photo.imageUrl(storageBaseUrl).orEmpty()
    Heading(photo.title,photo.album)
    SubcomposeAsyncImage(model=imageUrl,contentDescription=photo.title,modifier=Modifier.fillMaxWidth().height(420.dp),contentScale=ContentScale.Fit,loading={CircularProgressIndicator()},error={Empty("Não foi possível carregar a imagem.")})
    OutlinedButton(onClick={share(context,"${photo.title}\n$imageUrl")}) {Text("Compartilhar foto")}
}
@Composable fun ContactScreen(s:AppState,vm:AppViewModel) = Page {
    val c=s.church
    val context=LocalContext.current
    Heading("Fale com a igreja",c.name)
    LoadStatus(s,"Igreja",vm::reload)
    if(c.phone.isBlank() && c.whatsapp.isBlank() && c.email.isBlank() && c.address.isBlank()) Empty("Os contatos ainda não foram publicados pela secretaria.")
    if(c.whatsapp.isNotBlank()) Panel("WhatsApp",c.whatsapp,{openLink(context,"https://wa.me/${c.whatsapp.filter {it.isDigit()}}")})
    if(c.phone.isNotBlank()) Panel("Telefone",c.phone,{openLink(context,"tel:${Uri.encode(c.phone)}")})
    if(c.email.isNotBlank()) Panel("E-mail",c.email,{openLink(context,"mailto:${Uri.encode(c.email)}")})
    if(c.address.isNotBlank()) Panel("Como chegar",c.address,{openLink(context,"geo:0,0?q=${Uri.encode(c.address)}")})
}
@Composable fun RegistrationsScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    Heading("Minhas inscrições")
    var cancelId by remember {mutableStateOf<String?>(null)}
    if(!s.loggedIn) {Button(onClick={go("auth")}) {Text("Entrar")}; return@Page}
    LoadStatus(s,"Inscrições",vm::reload)
    val own=s.registrations.filter {it.userId==s.profile?.id}
    if(own.isEmpty() && !s.loading) Empty("Você ainda não tem inscrições.")
    own.forEach { r -> Panel(s.events.find {it.id==r.eventId}?.title ?: "Evento",r.name,{go("event/${r.eventId}")}) {
        TextButton(onClick={cancelId=r.id},enabled=!s.saving) {Text("Cancelar inscrição")}
    } }
    cancelId?.let { id -> AlertDialog(onDismissRequest={cancelId=null},title={Text("Cancelar inscrição?")},text={Text("Você poderá se inscrever novamente se o evento ainda estiver disponível.")},confirmButton={TextButton(onClick={cancelId=null; vm.action("Inscrição cancelada.") {registrations.cancel(id)}}) {Text("Cancelar inscrição")}},dismissButton={TextButton(onClick={cancelId=null}) {Text("Manter")}}) }
}
