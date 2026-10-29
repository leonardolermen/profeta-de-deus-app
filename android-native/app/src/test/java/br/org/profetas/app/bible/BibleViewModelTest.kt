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
import org.junit.Assert.assertTrue
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

    private fun immediateSource() = object : BibleSource {
        override suspend fun books() = listOf(psalms)
        override suspend fun chapter(book: Book, number: Int) = Chapter(book.name, number,
            listOf(Verse(1, "Primeiro"), Verse(2, "Segundo"), Verse(3, "Terceiro")))
    }

    @Test fun opensTheRequestedVerseWithoutScrollingThroughEarlierChapters() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = BibleViewModel(immediateSource(), FavoriteVerses(InMemoryPreferences()) { "visitante" })
        vm.openPassage(BiblePassage(psalms, 119, 3))
        advanceUntilIdle()
        assertEquals(119, vm.state.value.chapter?.number)
        assertEquals(3, vm.state.value.selectedVerse)
    }

    @Test fun absentVerseProducesHelpfulFeedbackAndKeepsChapterReadable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = BibleViewModel(immediateSource(), FavoriteVerses(InMemoryPreferences()) { "visitante" })
        vm.openPassage(BiblePassage(psalms, 119, 999))
        advanceUntilIdle()
        assertEquals(1, vm.state.value.selectedVerse)
        assertTrue(vm.state.value.referenceFailure.orEmpty().contains("999"))
        assertEquals(3, vm.state.value.chapter?.verses?.size)
    }

    @Test fun verseNavigationStopsAtChapterBoundaries() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = BibleViewModel(immediateSource(), FavoriteVerses(InMemoryPreferences()) { "visitante" })
        vm.openChapter(psalms, 1)
        advanceUntilIdle()
        vm.moveVerse(-1)
        assertEquals(1, vm.state.value.selectedVerse)
        repeat(5) { vm.moveVerse(1) }
        assertEquals(3, vm.state.value.selectedVerse)
        vm.moveVerse(-1)
        assertEquals(2, vm.state.value.selectedVerse)
    }

    @Test fun readingPreferencesSurviveAnotherViewModelAndFontSizeHasLimits() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = InMemoryPreferences()
        val favorites = FavoriteVerses(preferences) { "visitante" }
        val vm = BibleViewModel(immediateSource(), favorites, BibleReadingPreferences(preferences))
        vm.changeFontSize(100)
        vm.setContinuousReading(true)
        val reopened = BibleViewModel(immediateSource(), favorites, BibleReadingPreferences(preferences))
        assertEquals(32, reopened.state.value.fontSize)
        assertTrue(reopened.state.value.continuousReading)
        reopened.changeFontSize(-100)
        assertEquals(18, reopened.state.value.fontSize)
    }

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
