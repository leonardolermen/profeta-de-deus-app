package br.org.profetas.app.bible

// Fronteira entre o ViewModel e a API da Bíblia: permite ao teste segurar uma resposta e soltá-la na ordem que quiser.
interface BibleSource {

    suspend fun books(): List<Book>

    suspend fun chapter(book: Book, number: Int): Chapter
}
