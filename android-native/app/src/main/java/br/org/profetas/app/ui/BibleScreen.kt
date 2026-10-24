package br.org.profetas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.org.profetas.app.*
import br.org.profetas.app.support.intent.share

@OptIn(ExperimentalLayoutApi::class)
@Composable fun BibleScreen(s:AppState,vm:AppViewModel) {
    var query by rememberSaveable {mutableStateOf("")}
    var abbrev by rememberSaveable {mutableStateOf<String?>(null)}
    var number by rememberSaveable {mutableIntStateOf(0)}
    var favoritesOnly by rememberSaveable {mutableStateOf(false)}
    val book=s.books.find {it.abbreviation==abbrev}
    val context=LocalContext.current
    LaunchedEffect(Unit) {vm.loadBooks()}
    LaunchedEffect(abbrev,number,s.books) {if(book!=null && number>0) vm.loadChapter(book,number)}
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)) {
        item {Heading("Bíblia Sagrada", "Nova Versão Internacional • Leitura online")}
        item {Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            TextButton(onClick={abbrev=null;number=0;favoritesOnly=false}) {Text("Livros")}
            if(book!=null) TextButton(onClick={number=0;favoritesOnly=false}) {Text(book.name)}
            FilterChip(selected=favoritesOnly,onClick={favoritesOnly=!favoritesOnly},label={Text("Favoritos")})
        }}
        if(s.bibleLoading) item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        s.bibleError?.let { error -> item {Panel("Falha ao carregar",error) {TextButton(onClick={if(book!=null && number>0) vm.loadChapter(book,number) else vm.loadBooks()}) {Text("Tentar novamente")}}} }
        if(favoritesOnly) {
            if(s.favorites.isEmpty()) item {Empty("Marque versículos durante a leitura para encontrá-los aqui.")}
            items(s.favorites.sorted(),key={it}) { favorite -> Panel(favorite.substringBefore('\n'),favorite.substringAfter('\n')) {
                TextButton(onClick={share(context,favorite)}) {Text("Compartilhar")}
                TextButton(onClick={vm.toggleFavorite(favorite)}) {Text("Remover favorito")}
            } }
        } else if(book==null) {
            item {Field("Buscar livro",query,{query=it})}
            items(s.books.filter {it.name.contains(query,true)},key={it.abbreviation}) { b -> Panel(b.name,"${b.chapters} capítulos",{abbrev=b.abbreviation;number=0}) }
        } else if(number==0) {
            item {Heading(book.name,"Escolha um capítulo")}
            item {FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                (1..book.chapters).forEach {chapter -> OutlinedButton(onClick={number=chapter}) {Text("$chapter")} }
            }}
        } else {
            item {Heading("${book.name} $number")}
            if(!s.bibleLoading && s.chapter?.book==book.name && s.chapter.number==number) {
                items(s.chapter.verses,key={it.number}) {verse ->
                    val favorite="${book.name} $number:${verse.number}\n${verse.text}"
                    Panel("${verse.number}",verse.text) {
                        Row {
                            TextButton(onClick={vm.toggleFavorite(favorite)}) {Text(if(favorite in s.favorites) "Remover favorito" else "Favoritar")}
                            TextButton(onClick={share(context,favorite)}) {Text("Compartilhar")}
                        }
                    }
                }
            }
            item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                OutlinedButton(enabled=number>1 && !s.bibleLoading,onClick={number--}) {Text("Anterior")}
                OutlinedButton(enabled=number<book.chapters && !s.bibleLoading,onClick={number++}) {Text("Próximo")}
            }}
        }
    }
}
