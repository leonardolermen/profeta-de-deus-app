package br.org.profetas.app.bible

import br.org.profetas.app.BuildConfig
import br.org.profetas.app.support.http.SupabaseClient
import org.json.JSONArray
import org.json.JSONObject

private const val BIBLE_API = "https://abibliadigital.api.br/api/"

class BibleRepository(
    private val client: SupabaseClient,
    private val token: String = BuildConfig.BIBLE_API_TOKEN,
) : BibleSource {

    override suspend fun books(): List<Book> {
        val array = JSONArray(request("books"))

        return (0 until array.length()).map { index -> array.getJSONObject(index).toBook() }
    }

    override suspend fun chapter(book: Book, number: Int): Chapter {
        val json = JSONObject(request("verses/nvi/${book.abbreviation}/$number"))
        val verses = json.getJSONArray("verses")

        return Chapter(
            book = book.name,
            number = number,
            verses = (0 until verses.length()).map { index -> verses.getJSONObject(index).toVerse() },
        )
    }

    private suspend fun request(path: String): String = client.external(BIBLE_API + path, token)

    private fun JSONObject.toBook(): Book = Book(
        name = getString("name"),
        abbreviation = getJSONObject("abbrev").getString("pt"),
        chapters = getInt("chapters"),
    )

    private fun JSONObject.toVerse(): Verse = Verse(number = getInt("number"), text = getString("text"))
}
