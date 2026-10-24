package br.org.profetas.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.org.profetas.app.*
import br.org.profetas.app.support.intent.openLink

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
