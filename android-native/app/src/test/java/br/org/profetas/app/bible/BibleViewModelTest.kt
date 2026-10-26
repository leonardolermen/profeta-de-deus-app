package br.org.profetas.app.bible

import br.org.profetas.app.support.storage.InMemoryPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleViewModelTest {

    // Cada capítulo só responde quando o teste completa a resposta dele: a ordem de chegada fica nas mãos do teste.
    private class HeldChapters : BibleSource {

        val responses = mapOf(1 to CompletableDeferred<Chapter>(), 2 to CompletableDeferred<Chapter>())

        override suspend fun books(): List<Book> = emptyList()

        override suspend fun chapter(book: Book, number: Int): Chapter = responses.getValue(number).await()

        fun answer(book: Book, number: Int) {
            val verses = listOf(Verse(number = 1, text = "Capítulo $number"))

            responses.getValue(number).complete(Chapter(book = book.name, number = number, verses = verses))
        }
    }

    private val psalms = Book(name = "Salmos", abbreviation = "sl", chapters = 150)

    @After fun resetMain() {
        Dispatchers.resetMain()
    }

    @Test fun staleChapterIsDiscarded() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = HeldChapters()
        val viewModel = BibleViewModel(source, FavoriteVerses(InMemoryPreferences()) { "visitante" })

        viewModel.openChapter(psalms, 1)
        viewModel.openChapter(psalms, 2)
        advanceUntilIdle()

        source.answer(psalms, 2)
        advanceUntilIdle()

        // A resposta do capítulo 1 chega por último, depois que o membro já está lendo o 2.
        source.answer(psalms, 1)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.chapter?.number)
        assertEquals("Capítulo 2", state.chapter?.verses?.single()?.text)
        assertFalse(state.isLoadingChapter)
    }
}
