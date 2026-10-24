package br.org.profetas.app.ui

import android.net.Uri
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
import br.org.profetas.app.*
import br.org.profetas.app.R
import br.org.profetas.app.support.intent.openLink
import br.org.profetas.app.support.intent.share
import coil.compose.SubcomposeAsyncImage
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

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
