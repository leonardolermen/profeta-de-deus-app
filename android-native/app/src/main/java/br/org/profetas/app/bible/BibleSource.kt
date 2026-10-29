package br.org.profetas.app.bible

// Fronteira entre o ViewModel e o texto bíblico: a implementação atual lê a edição local licenciada.
interface BibleSource {

    suspend fun books(): List<Book>

    suspend fun chapter(book: Book, number: Int): Chapter
}
