package br.org.profetas.app.bible

import android.content.SharedPreferences
import br.org.profetas.app.support.storage.AccountScopedSet

// Versículos favoritos, guardados no aparelho por conta. A chave do versículo é "Livro capítulo:número\ntexto":
// a referência e o texto juntos, para a lista de favoritos não precisar consultar a API de novo.
class FavoriteVerses(preferences: SharedPreferences, accountId: () -> String) {

    private val stored = AccountScopedSet(preferences, "favorites", accountId)

    fun all(): Set<String> = stored.all()

    fun toggle(verseKey: String) {
        stored.toggle(verseKey)
    }

    companion object {

        fun keyOf(book: String, chapter: Int, verse: Verse): String = "$book $chapter:${verse.number}\n${verse.text}"

        fun referenceOf(verseKey: String): String = verseKey.substringBefore('\n')

        fun textOf(verseKey: String): String = verseKey.substringAfter('\n')
    }
}
