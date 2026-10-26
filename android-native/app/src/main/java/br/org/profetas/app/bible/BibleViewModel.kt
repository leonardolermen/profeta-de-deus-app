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
) {
    val isBusy: Boolean get() = isLoadingBooks || isLoadingChapter
}

class BibleViewModel(private val bible: BibleSource, private val favorites: FavoriteVerses) : ViewModel() {

    private val mutableState = MutableStateFlow(BibleState(favorites = favorites.all()))

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
        val generation = ++chapterGeneration

        mutableState.update { current ->
            current.copy(
                isLoadingChapter = true,
                failure = null,
                opened = ChapterRequest(book, number),
                chapter = null,
            )
        }

        viewModelScope.launch {
            try {
                val chapter = bible.chapter(book, number)

                if (generation == chapterGeneration) {
                    mutableState.update { current -> current.copy(isLoadingChapter = false, chapter = chapter) }
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
            current.copy(isLoadingChapter = false, failure = null, opened = null, chapter = null)
        }
    }

    // Puxar para atualizar e "Tentar novamente": refaz o capítulo aberto, ou a lista de livros se nenhum estiver.
    fun reload() {
        val opened = state.value.opened

        if (opened == null) {
            loadBooks()
        } else {
            openChapter(opened.book, opened.number)
        }
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

                BibleViewModel(bible = container.bible, favorites = container.favoriteVerses)
            }
        }
    }
}
