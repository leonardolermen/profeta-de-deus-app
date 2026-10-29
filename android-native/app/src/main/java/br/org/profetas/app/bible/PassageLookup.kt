package br.org.profetas.app.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
internal fun PassageLookup(
    books: List<Book>,
    initial: BiblePassage? = null,
    isLoading: Boolean = false,
    showBooksInline: Boolean = false,
    onOpen: (BiblePassage) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf(initial?.book?.name.orEmpty()) }
    var selected by rememberSaveable { mutableStateOf(initial?.book?.abbreviation) }
    var chapter by rememberSaveable { mutableStateOf(initial?.chapter?.toString() ?: "1") }
    var verse by rememberSaveable { mutableStateOf(initial?.verse?.toString().orEmpty()) }
    var failure by rememberSaveable { mutableStateOf<String?>(null) }
    var showingBooks by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val book = books.find { it.abbreviation == selected }
    fun choose(value: Book) {
        selected = value.abbreviation
        query = value.name
        chapter = "1"
        verse = ""
        failure = null
        keyboard?.hide()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; selected = null; failure = null },
            label = { Text("Pesquisar livro", fontSize = 16.sp) },
            placeholder = { Text("Ex.: João ou Salmos") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
        )
        if (book == null && (query.isNotBlank() || showBooksInline)) {
            val matches = books.filter { it.matchesBookSearch(query) }
            if (showBooksInline) Text("Escolha um livro", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            (if (showBooksInline) matches else matches.take(5)).forEach { candidate ->
                OutlinedCard(onClick = { choose(candidate) }, modifier = Modifier.fillMaxWidth()) {
                    Text(candidate.name, modifier = Modifier.padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (matches.isEmpty() && !isLoading) Text("Nenhum livro encontrado. Confira o nome.")
        }
        if (!showBooksInline || book != null) TextButton(onClick = {
            if (showBooksInline) { selected = null; query = ""; failure = null } else showingBooks = true
        }, enabled = books.isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) {
            Text(if (book == null) "Ver todos os livros" else "Trocar livro", fontSize = 17.sp)
        }
        if (!showBooksInline || book != null) {
        if (showBooksInline) Text(book!!.name, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = chapter, onValueChange = { chapter = it; failure = null },
                label = { Text("Capítulo") },
                supportingText = { Text(book?.let { "De 1 a ${it.chapters}" } ?: "Escolha o livro") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
            )
            OutlinedTextField(
                value = verse, onValueChange = { verse = it; failure = null },
                label = { Text("Versículo") }, supportingText = { Text("Opcional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
            )
        }
        failure?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 16.sp) }
        Button(
            onClick = {
                try {
                    val passage = BiblePassage.parse(requireNotNull(book), chapter, verse)
                    keyboard?.hide()
                    onOpen(passage)
                } catch (e: IllegalArgumentException) { failure = e.message }
            },
            enabled = book != null && !isLoading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) { Text("Ler passagem", fontSize = 19.sp, fontWeight = FontWeight.Bold) }
        }
    }

    if (showingBooks) BookPickerDialog(books, onDismiss = { showingBooks = false }) {
        choose(it)
        showingBooks = false
    }
}

@Composable
private fun BookPickerDialog(books: List<Book>, onDismiss: () -> Unit, onChoose: (Book) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Escolha o livro", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(query, { query = it }, label = { Text("Pesquisar livro") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val matches = books.filter { it.matchesBookSearch(query) }
                    if (matches.isEmpty()) item { Text("Nenhum livro encontrado.") }
                    items(matches, key = { it.abbreviation }) { book ->
                        OutlinedCard(onClick = { onChoose(book) }, modifier = Modifier.fillMaxWidth()) {
                            Text(book.name, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Fechar", fontSize = 17.sp) }
            }
        }
    }
}
