package br.org.profetas.app.bible

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class BibleRepositoryTest {
    private val assets = File("src/main/assets")
    private val source = BibleRepository { File(assets, it).inputStream() }

    @Test fun bundledEditionMatchesOfficialReleaseAndContainsAllChapters() = runTest {
        val file = File(assets, "bible/blivre-tr-2018.txt")
        val hash = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals("43e41dc7a979c2f668fce941d10c2ecd564912a3a02554706f78d66602248d52", hash)
        val lines = file.readLines().map { it.removePrefix("\uFEFF") }
        assertEquals(31102, lines.size)
        assertEquals(1189, lines.map { it.substringBefore(':') }.distinct().size)
        assertEquals(66, source.books().size)
        assertEquals(1189, source.books().sumOf { it.chapters })
        assertEquals(1, source.chapter(source.books().first(), 1).verses.first().number)
        assertEquals("No princípio criou Deus os céus e a terra.", source.chapter(source.books().first(), 1).verses.first().text)
    }

    @Test fun readsOriginalTextWithoutNetworkOrEditorialChanges() = runTest {
        val john = source.books().single { it.abbreviation == "jo" }
        val chapter = source.chapter(john, 3)
        assertEquals(36, chapter.verses.size)
        assertEquals("Porque Deus amou ao mundo de tal maneira, que deu o seu Filho unigênito; para que todo aquele que nele crê não pereça, mas tenha a vida eterna.", chapter.verses.single { it.number == 16 }.text)
    }

    @Test fun chapterPrefixDoesNotConfuseOneWithTenOrOneHundred() = runTest {
        val psalms = source.books().single { it.abbreviation == "sl" }
        assertEquals(6, source.chapter(psalms, 1).verses.size)
        assertEquals(176, source.chapter(psalms, 119).verses.size)
        assertEquals(6, source.chapter(psalms, 150).verses.size)
    }

    @Test fun catalogUsesChapterCountsFromTheOfficialText() = runTest {
        val books = source.books()
        assertEquals(3, books.single { it.name == "Tito" }.chapters)
        assertEquals(1, books.single { it.name == "Filemom" }.chapters)
        assertEquals(21, source.chapter(books.last(), 22).verses.last().number)
    }

    @Test fun rejectsInvalidChapterInsteadOfReturningAnotherPassage() = runTest {
        val genesis = source.books().first()
        try {
            source.chapter(genesis, 51)
            fail("Expected invalid chapter")
        } catch (_: IllegalArgumentException) {}
    }

    @Test fun sharingIncludesTheActualEditionAndLicense() {
        val key = FavoriteVerses.keyOf("João", 3, Verse(16, "Texto de teste"))
        assertTrue(FavoriteVerses.isCurrentEdition(key))
        assertTrue(BibleEdition.shareText(key).contains(BibleEdition.LICENSE_URL))
        assertTrue(BibleEdition.shareText(key).contains("Diego Santos"))
    }

    @Test fun previousFavoritesKeepTheirReferencesAndCanOpenTheNewEdition() = runTest {
        val old = "João 3:16\nTexto anteriormente salvo"
        assertFalse(FavoriteVerses.isCurrentEdition(old))
        val passage = requireNotNull(FavoriteVerses.passageOf(old, source.books()))
        assertEquals(3, passage.chapter)
        assertEquals(16, passage.verse)
    }
}
