package br.org.profetas.app.bible

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStream

/** Texto oficial BLIVRE TR 2018.2.0, distribuído com licença e créditos no APK. */
class BibleRepository(private val openAsset: (String) -> InputStream) : BibleSource {
    private data class Entry(val book: Book, val code: String)
    private val catalog by lazy {
        val json = openAsset("bible/books.json").bufferedReader(Charsets.UTF_8).use { JSONArray(it.readText()) }
        (0 until json.length()).map { i ->
            val row = json.getJSONObject(i)
            Entry(Book(row.getString("name"), row.getString("abbreviation"), row.getInt("chapters")), row.getString("code"))
        }
    }

    override suspend fun books(): List<Book> = withContext(Dispatchers.IO) { catalog.map { it.book } }

    override suspend fun chapter(book: Book, number: Int): Chapter = withContext(Dispatchers.IO) {
        val entry = requireNotNull(catalog.find { it.book.abbreviation == book.abbreviation }) { "Livro não encontrado." }
        require(number in 1..entry.book.chapters) { "Capítulo inválido." }
        val prefix = "${entry.code} $number:"
        val verses = openAsset("bible/blivre-tr-2018.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.map { it.removePrefix("\uFEFF") }.filter { it.startsWith(prefix) }.map { line ->
                val start = line.indexOf(' ', prefix.length)
                Verse(line.substring(prefix.length, start).toInt(), line.substring(start + 1))
            }.toList()
        }
        check(verses.isNotEmpty()) { "Texto do capítulo indisponível." }
        Chapter(entry.book.name, number, verses)
    }
}
