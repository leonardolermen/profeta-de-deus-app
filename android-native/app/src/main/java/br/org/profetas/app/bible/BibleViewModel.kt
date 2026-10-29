package br.org.profetas.app.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChapterRequest(val book: Book, val number: Int)

data class BibleState(
    val isLoadingBooks: Boolean = false,
    val isLoadingChapter: Boolean = false,
    val failure: String? = null,
    val books: List<Book> = emptyList(),
    val opened: ChapterRequest? = null,
    val chapter: Chapter? = null,
    val favorites: Set<String> = emptySet(),
    val selectedVerse: Int = 1,
    val requestedVerse: Int? = null,
    val referenceFailure: String? = null,
    val fontSize: Int = 22,
    val continuousReading: Boolean = false,
) {
    val isBusy: Boolean get() = isLoadingBooks || isLoadingChapter
}

class BibleViewModel(
    private val bible: BibleSource,
    private val favorites: FavoriteVerses,
    private val readingPreferences: BibleReadingPreferences? = null,
) : ViewModel() {

    private val mutableState = MutableStateFlow(BibleState(
        favorites = favorites.all(),
        fontSize = readingPreferences?.fontSize() ?: 22,
        continuousReading = readingPreferences?.continuous() ?: false,
    ))

    val state: StateFlow<BibleState> = mutableState.asStateFlow()

    // Cada capítulo pedido ganha um número; a resposta de um pedido que já foi substituído é descartada. Sem isso,
    // tocar no capítulo 1 e logo no 2 podia terminar mostrando o 1, se a API respondesse o 1 por último.
    private var chapterGeneration = 0

    fun loadBooks() {
        if (state.value.books.isNotEmpty() || state.value.isLoadingBooks) {
            return
        }

        mutableState.update { current -> current.copy(isLoadingBooks = true, failure = null) }

        viewModelScope.launch {
            try {
                val books = bible.books()

                mutableState.update { current -> current.copy(isLoadingBooks = false, books = books) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                val message = e.message ?: "Não foi possível consultar os livros."
                mutableState.update { current -> current.copy(isLoadingBooks = false, failure = message) }
            }
        }
    }

    fun openChapter(book: Book, number: Int) {
        openPassage(BiblePassage(book, number))
    }

    fun openPassage(passage: BiblePassage) {
        val book = passage.book
        val number = passage.chapter
        if (number !in 1..book.chapters || (passage.verse != null && passage.verse < 1)) {
            mutableState.update { it.copy(referenceFailure = "Confira o capítulo e o versículo informados.") }
            return
        }
        val generation = ++chapterGeneration

        mutableState.update { current ->
            current.copy(
                isLoadingChapter = true,
                failure = null,
                opened = ChapterRequest(book, number),
                chapter = null,
                requestedVerse = passage.verse,
                selectedVerse = passage.verse ?: 1,
                referenceFailure = null,
            )
        }

        viewModelScope.launch {
            try {
                val chapter = bible.chapter(book, number)

                if (generation == chapterGeneration) {
                    val target = chapter.verses.find { it.number == passage.verse }
                    val missing = passage.verse != null && target == null
                    mutableState.update { current -> current.copy(
                        isLoadingChapter = false,
                        chapter = chapter,
                        selectedVerse = target?.number ?: chapter.verses.firstOrNull()?.number ?: 1,
                        referenceFailure = if (missing) "${book.name} $number não possui o versículo ${passage.verse}. Escolha um dos ${chapter.verses.size} versículos deste capítulo." else null,
                    ) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                if (generation == chapterGeneration) {
                    val message = e.message ?: "Não foi possível carregar o capítulo."
                    mutableState.update { current -> current.copy(isLoadingChapter = false, failure = message) }
                }
            }
        }
    }

    fun closeChapter() {
        chapterGeneration++

        mutableState.update { current ->
            current.copy(isLoadingChapter = false, failure = null, referenceFailure = null, opened = null, chapter = null)
        }
    }

    // Puxar para atualizar e "Tentar novamente": refaz o capítulo aberto, ou a lista de livros se nenhum estiver.
    fun reload() {
        val opened = state.value.opened

        if (opened == null) {
            loadBooks()
        } else {
            openPassage(BiblePassage(opened.book, opened.number, state.value.selectedVerse))
        }
    }

    fun selectVerse(number: Int) {
        val chapter = state.value.chapter ?: return
        if (chapter.verses.any { it.number == number }) {
            mutableState.update { it.copy(selectedVerse = number, referenceFailure = null) }
        }
    }

    fun moveVerse(offset: Int) {
        val verses = state.value.chapter?.verses ?: return
        val index = verses.indexOfFirst { it.number == state.value.selectedVerse }
        verses.getOrNull(index + offset)?.let { selectVerse(it.number) }
    }

    fun moveChapter(offset: Int) {
        val opened = state.value.opened ?: return
        val number = opened.number + offset
        if (number in 1..opened.book.chapters) openChapter(opened.book, number)
    }

    fun changeFontSize(offset: Int) {
        val size = (state.value.fontSize + offset).coerceIn(18, 32)
        readingPreferences?.saveFontSize(size)
        mutableState.update { it.copy(fontSize = size) }
    }

    fun setContinuousReading(value: Boolean) {
        readingPreferences?.saveContinuous(value)
        mutableState.update { it.copy(continuousReading = value) }
    }

    fun toggleFavorite(verseKey: String) {
        favorites.toggle(verseKey)

        refreshFavorites()
    }

    // Os favoritos são por conta: quem entra ou sai da conta passa a ver os do novo dono.
    fun refreshFavorites() {
        mutableState.update { current -> current.copy(favorites = favorites.all()) }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                BibleViewModel(
                    bible = container.bible,
                    favorites = container.favoriteVerses,
                    readingPreferences = BibleReadingPreferences(application.getSharedPreferences("preferences", 0)),
                )
            }
        }
    }
}
