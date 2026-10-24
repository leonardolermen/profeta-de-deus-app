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
