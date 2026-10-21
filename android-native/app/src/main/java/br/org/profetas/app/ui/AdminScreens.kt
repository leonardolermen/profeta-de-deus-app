package br.org.profetas.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.org.profetas.app.*
import br.org.profetas.app.data.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable fun AdminScreen(s:AppState,vm:AppViewModel,go:(String)->Unit) = Page {
    if(!s.isAdmin) {Empty("Acesso restrito à administração."); return@Page}
    Heading("Administração", "Conteúdo real para a comunidade.")
    var section by rememberSaveable {mutableStateOf("eventos")}
    var query by rememberSaveable {mutableStateOf("")}
    var deleting by remember {mutableStateOf<Pair<String,String>?>(null)}
    val labels=linkedMapOf("eventos" to "Agenda","estudos" to "Estudos","fotos" to "Fotos","aniversariantes" to "Aniversários","igreja" to "Igreja","oracoes" to "Orações","inscricoes" to "Inscrições")
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {labels.forEach {(key,label)->FilterChip(selected=section==key,onClick={section=key;query=""},label={Text(label)})}}
    Field("Buscar nesta seção",query,{query=it})
    if(section !in listOf("oracoes","inscricoes","igreja")) Button(onClick={go("editor/$section/new")}) {Text("Novo cadastro")}
    s.errors[labels[section]]?.let {Empty(it)}
    when(section) {
        "eventos" -> s.events.filter {it.title.contains(query,true)}.forEach {e-> AdminItem(e.title,displayDate(e.date),{go("editor/eventos/${e.id}")},{deleting="eventos" to e.id})}
        "estudos" -> s.studies.filter {it.title.contains(query,true)}.forEach {e-> AdminItem(e.title,e.category,{go("editor/estudos/${e.id}")},{deleting="estudos" to e.id})}
        "fotos" -> s.photos.filter {"${it.title} ${it.album}".contains(query,true)}.forEach {e-> AdminItem(e.title,e.album,{go("editor/fotos/${e.id}")},{deleting="fotos" to e.id})}
        "aniversariantes" -> s.birthdays.filter {it.name.contains(query,true)}.forEach {e-> AdminItem(e.name,"${e.day}/${e.month}",{go("editor/aniversariantes/${e.id}")},{deleting="aniversariantes" to e.id})}
        "igreja" -> Panel(s.church.name,"Contatos, história e política de privacidade.") {Button(onClick={go("editor/igreja/1")}) {Text("Editar informações")}}
        "oracoes" -> s.prayers.filter {"${it.name} ${it.text}".contains(query,true)}.forEach {e-> Panel(e.name,"${displayDate(e.date)}\n\n${e.text}") {TextButton(onClick={deleting="oracoes" to e.id}) {Text("Excluir pedido")}}}
        "inscricoes" -> s.registrations.filter {"${it.name} ${s.events.find {e->e.id==it.eventId}?.title}".contains(query,true)}.forEach {e->Panel(e.name,"${s.events.find {it.id==e.eventId}?.title ?: "Evento"}\n${e.phone}") {
            TextButton(onClick={deleting="inscricoes" to e.id}) {Text("Cancelar inscrição")}
        }}
    }
    if(!s.loading) Text("Use Atualizar no topo para buscar os cadastros mais recentes.",style=MaterialTheme.typography.bodySmall)
    deleting?.let {(table,id)->AlertDialog(onDismissRequest={deleting=null},title={Text("Excluir este registro?")},text={Text(if(table=="eventos") "O evento e suas inscrições serão excluídos. Esta ação não pode ser desfeita." else "Esta ação não pode ser desfeita.")},confirmButton={TextButton(enabled=!s.saving,onClick={deleting=null;vm.delete(table,id)}) {Text("Excluir")}},dismissButton={TextButton(onClick={deleting=null}) {Text("Voltar")}})}
}
@Composable private fun AdminItem(title:String,subtitle:String,edit:()->Unit,delete:()->Unit) = Panel(title,subtitle) {
    Row {TextButton(onClick=edit) {Text("Editar")};TextButton(onClick=delete) {Text("Excluir")}}
}
@Composable fun EditorScreen(s:AppState,vm:AppViewModel,table:String,id:String,done:()->Unit) = Page {
    if(!s.isAdmin) {Empty("Acesso restrito à administração."); return@Page}
    val existing=id!="new"
    val initial=remember(table,id) {
        when(table) {
            "eventos" -> s.events.find {it.id==id}?.let {e->mapOf("titulo" to e.title,"descricao" to e.description,"tipo" to e.type,"data" to (eventInstant(e.date)?.atZone(ZoneId.systemDefault())?.toLocalDateTime()?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: ""))}
            "estudos" -> s.studies.find {it.id==id}?.let {mapOf("titulo" to it.title,"descricao" to it.description,"categoria" to it.category,"conteudo" to it.content)}
            "fotos" -> s.photos.find {it.id==id}?.let {mapOf("titulo" to it.title,"url" to it.url,"album" to it.album)}
            "aniversariantes" -> s.birthdays.find {it.id==id}?.let {mapOf("nome" to it.name,"dia" to "${it.day}","mes" to "${it.month}","consentimento" to "${it.consent}")}
            "igreja" -> s.church.let {mapOf("nome" to it.name,"telefone" to it.phone,"whatsapp" to it.whatsapp,"email" to it.email,"endereco" to it.address,"historia" to it.history,"privacidade" to it.privacy)}
            else -> null
        } ?: emptyMap()
    }
    var values by rememberSaveable(table,id) {mutableStateOf(initial)}
    var validation by remember {mutableStateOf<String?>(null)}
    fun value(key:String)=values[key].orEmpty()
    fun change(key:String,text:String) {values=values+(key to text);validation=null}
    Heading(if(existing) "Editar cadastro" else "Novo cadastro")
    if(existing && table!="igreja" && initial.isEmpty()) {Empty("Registro indisponível. Volte e atualize os dados.");return@Page}
    val fields=when(table) {
        "eventos" -> listOf("titulo" to "Título","descricao" to "Descrição","data" to "Data e hora: aaaa-MM-dd HH:mm")
        "estudos" -> listOf("titulo" to "Título","descricao" to "Resumo","categoria" to "Categoria","conteudo" to "Conteúdo completo")
        "fotos" -> listOf("titulo" to "Título","url" to "URL pública HTTPS da foto","album" to "Álbum")
        "aniversariantes" -> listOf("nome" to "Nome","dia" to "Dia","mes" to "Mês (1 a 12)")
        "igreja" -> listOf("nome" to "Nome da igreja","telefone" to "Telefone com DDD","whatsapp" to "WhatsApp com país e DDD (55…)","email" to "E-mail","endereco" to "Endereço completo","historia" to "História e missão","privacidade" to "Política de privacidade e canal para solicitações")
        else -> emptyList()
    }
    fields.forEach {(key,label)->Field(label,value(key),{change(key,it.take(if(key in listOf("conteudo","historia","privacidade")) 50000 else if(key=="descricao") 4000 else 1000))},key in listOf("descricao","conteudo","historia","privacidade"))}
    if(table=="eventos") {
        Text("Horário local: ${ZoneId.systemDefault().id}. Será salvo com fuso horário.")
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("culto","campanha","batismo").forEach {type->FilterChip(selected=value("tipo").ifBlank {"culto"}==type,onClick={change("tipo",type)},label={Text(type)})}}
    }
    if(table=="aniversariantes") Row {Checkbox(value("consentimento")=="true",{change("consentimento","$it")});Text("A pessoa autorizou a divulgação do nome e dia/mês aos membros.",Modifier.weight(1f).padding(top=10.dp))}
    if(table=="fotos") Text("Publique somente imagens autorizadas. Use uma URL pública HTTPS do armazenamento de sua escolha.",style=MaterialTheme.typography.bodySmall)
    validation?.let {Text(it,color=MaterialTheme.colorScheme.error)}
    Button(enabled=!s.saving && fields.isNotEmpty(),onClick={
        try {
            val body=JSONObject()
            fields.forEach {(key,_)->if(key!="data") body.put(key,value(key).trim())}
            val title=value(if(table in listOf("aniversariantes","igreja")) "nome" else "titulo").trim()
            require(title.length in 2..if(table in listOf("aniversariantes","igreja")) 120 else 160) {"Informe um nome ou título entre 2 e 120/160 caracteres."}
            when(table) {
                "eventos" -> {
                    val local=LocalDateTime.parse(value("data").trim(),DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(java.time.format.ResolverStyle.STRICT))
                    body.put("data_evento",local.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString())
                    body.put("tipo",value("tipo").ifBlank {"culto"})
                }
                "estudos" -> require(value("conteudo").trim().length>=5) {"Escreva o conteúdo do estudo."}
                "fotos" -> require(validHttps(value("url").trim())) {"Informe uma URL HTTPS válida."}
                "aniversariantes" -> {
                    val day=value("dia").toIntOrNull() ?: error("Informe o dia.")
                    val month=value("mes").toIntOrNull() ?: error("Informe o mês.")
                    MonthDay.of(month,day)
                    body.put("dia",day).put("mes",month).put("consentimento",value("consentimento")=="true")
                }
                "igreja" -> {
                    require(value("email").isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(value("email").trim()).matches()) {"Informe um e-mail válido."}
                    require(value("whatsapp").isBlank() || value("whatsapp").count {it.isDigit()} in 10..15) {"Informe o WhatsApp com país e DDD."}
                }
            }
            vm.save(table,if(existing) id else null,body,done)
        } catch(e:Exception) {validation=e.message ?: "Confira os campos do formulário."}
    }) {Text("Salvar cadastro")}
}
