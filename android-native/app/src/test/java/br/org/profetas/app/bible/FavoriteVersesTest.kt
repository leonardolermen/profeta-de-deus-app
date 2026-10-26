package br.org.profetas.app.bible

import br.org.profetas.app.support.storage.InMemoryPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteVersesTest {

    private val verse = "Salmos 23:1\nO Senhor é o meu pastor; de nada terei falta."

    @Test fun separatedByAccount() {
        var account = "a"
        val favorites = FavoriteVerses(InMemoryPreferences()) { account }

        favorites.toggle(verse)

        account = "b"
        assertEquals(emptySet<String>(), favorites.all())

        account = "a"
        assertTrue(verse in favorites.all())
    }

    // Quem já usa o app guardou os favoritos sob "favorites:<conta>"; a tela nova tem que continuar achando.
    @Test fun keepsExistingKeyFormat() {
        val preferences = InMemoryPreferences(mapOf("favorites:visitante" to setOf(verse)))

        val favorites = FavoriteVerses(preferences) { "visitante" }

        assertTrue(verse in favorites.all())
    }
}
