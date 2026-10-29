package br.org.profetas.app.bible

import java.text.Normalizer
import java.util.Locale

private fun String.bookSearchKey(): String = Normalizer.normalize(trim(), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}"), "")
    .lowercase(Locale.ROOT)
    .replace(Regex("\\s+"), "")

fun Book.matchesBookSearch(query: String): Boolean {
    val key = query.bookSearchKey()
    return name.bookSearchKey().contains(key) || abbreviation.bookSearchKey().contains(key)
}

data class BiblePassage(val book: Book, val chapter: Int, val verse: Int? = null) {
    companion object {
        fun parse(book: Book, chapterText: String, verseText: String): BiblePassage {
            val chapter = chapterText.trim().toIntOrNull()
            require(chapter != null && chapter in 1..book.chapters) {
                "Escolha um capítulo de 1 a ${book.chapters} para ${book.name}."
            }
            val verse = if (verseText.isBlank()) null else verseText.trim().toIntOrNull()
            require(verseText.isBlank() || (verse != null && verse > 0)) {
                "Informe um versículo maior que zero ou deixe o campo vazio."
            }
            return BiblePassage(book, chapter, verse)
        }
    }
}
