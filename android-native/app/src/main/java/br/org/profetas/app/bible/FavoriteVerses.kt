package br.org.profetas.app.bible

import android.content.SharedPreferences
import br.org.profetas.app.support.storage.AccountScopedSet

// Favoritos por conta. Os novos registros identificam BLIVRE; referências anteriores continuam disponíveis,
// sem apresentar o texto da tradução antiga como se pertencesse à edição atual.
class FavoriteVerses(preferences: SharedPreferences, accountId: () -> String) {

    private val stored = AccountScopedSet(preferences, "favorites", accountId)

    fun all(): Set<String> = stored.all()

    fun toggle(verseKey: String) {
        stored.toggle(verseKey)
    }

    companion object {

        fun keyOf(book: String, chapter: Int, verse: Verse): String = "$book $chapter:${verse.number} (BLIVRE)\n${verse.text}"

        fun isCurrentEdition(verseKey: String): Boolean = referenceOf(verseKey).endsWith(" (BLIVRE)")

        fun passageOf(verseKey: String, books: List<Book>): BiblePassage? {
            val reference = referenceOf(verseKey).removeSuffix(" (BLIVRE)")
            val match = Regex("^(.*) (\\d+):(\\d+)$").matchEntire(reference) ?: return null
            val book = books.find { it.name == match.groupValues[1] } ?: return null
            return runCatching { BiblePassage.parse(book, match.groupValues[2], match.groupValues[3]) }.getOrNull()
        }

        fun referenceOf(verseKey: String): String = verseKey.substringBefore('\n')

        fun textOf(verseKey: String): String = verseKey.substringAfter('\n')
    }
}
