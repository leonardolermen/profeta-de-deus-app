package br.org.profetas.app.bible

import org.junit.Assert.*
import org.junit.Test

class BiblePassageTest {
    private val john = Book("João", "jo", 21)

    @Test fun searchIgnoresAccentsAndCase() {
        assertTrue(john.matchesBookSearch("JOAO"))
        assertTrue(john.matchesBookSearch(" jo "))
        assertFalse(john.matchesBookSearch("salmos"))
    }

    @Test fun numberedBooksCanBeFoundWithoutSpacesOrAccents() {
        assertTrue(Book("1 João", "1jo", 5).matchesBookSearch("1joao"))
        assertTrue(Book("1 João", "1jo", 5).matchesBookSearch("1 jo"))
    }

    @Test fun verseIsOptionalWhenOpeningAChapter() {
        assertEquals(BiblePassage(john, 3, null), BiblePassage.parse(john, " 3 ", ""))
    }

    @Test fun exactReferenceKeepsTheRequestedVerse() {
        assertEquals(BiblePassage(john, 3, 16), BiblePassage.parse(john, "3", "16"))
    }

    @Test(expected = IllegalArgumentException::class) fun chapterBeyondBookIsRejected() {
        BiblePassage.parse(john, "22", "1")
    }

    @Test(expected = IllegalArgumentException::class) fun chapterZeroIsRejected() {
        BiblePassage.parse(john, "0", "")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidNumberIsRejected() {
        BiblePassage.parse(john, "abc", "16")
    }

    @Test(expected = IllegalArgumentException::class) fun verseZeroIsRejected() {
        BiblePassage.parse(john, "3", "0")
    }

    @Test(expected = IllegalArgumentException::class) fun veryLargeVerseIsRejectedWithoutOverflow() {
        BiblePassage.parse(john, "3", "99999999999999999999")
    }
}
