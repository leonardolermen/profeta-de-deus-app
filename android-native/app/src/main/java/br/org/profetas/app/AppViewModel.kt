package br.org.profetas.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.org.profetas.app.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class AppState(
    val loading: Boolean = false, val saving: Boolean = false, val errors: Map<String,String> = emptyMap(),
    val message: String? = null, val events: List<Event> = emptyList(), val studies: List<Study> = emptyList(),
    val birthdays: List<Birthday> = emptyList(), val photos: List<Photo> = emptyList(), val church: Church = Church(),
    val profile: Profile? = null, val loggedIn: Boolean = false, val email: String = "",
    val registrations: List<Registration> = emptyList(), val prayers: List<Prayer> = emptyList(),
    val books: List<Book> = emptyList(), val chapter: Chapter? = null, val bibleLoading: Boolean = false,
    val bibleError: String? = null, val dark: Boolean = false, val completed: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(), val reminders: Set<String> = emptySet()
) {
    val isAdmin: Boolean get() = loggedIn && ContentAccess.canManage(profile)
}
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val prefs = app.getSharedPreferences("preferences", 0)
    private val mutable = MutableStateFlow(AppState(dark = prefs.getBoolean("dark", false)))
    val state = mutable.asStateFlow()
    val configured get() = repo.configured
    private var chapterGeneration = 0
    init { reload() }
    fun dismissMessage() { mutable.update { it.copy(message = null) } }
    fun message(text: String) { mutable.update { it.copy(message = text) } }
    fun setDark(value: Boolean) { prefs.edit().putBoolean("dark", value).apply(); mutable.update { it.copy(dark = value) } }
    private fun localKey(type: String) = "$type:${repo.session?.userId ?: "visitante"}"
    private fun readSet(type: String) = prefs.getStringSet(localKey(type), emptySet())!!.toSet()
    fun toggleStudy(id: String) { toggle("studies", id); mutable.update { it.copy(completed = readSet("studies")) } }
    fun toggleFavorite(id: String) { toggle("favorites", id); mutable.update { it.copy(favorites = readSet("favorites")) } }
    private fun toggle(type: String, id: String) {
        val values = readSet(type).toMutableSet(); if (!values.add(id)) values.remove(id)
        prefs.edit().putStringSet(localKey(type), values).apply()
    }
    fun reminder(event: Event, enabled: Boolean) {
        if (enabled) EventReminder.schedule(getApplication(), event) else EventReminder.cancel(getApplication(), event.id)
        val values = state.value.reminders.toMutableSet()
        if (enabled) values.add(event.id) else values.remove(event.id)
        prefs.edit().putStringSet("reminders", values).apply()
        mutable.update { it.copy(reminders = values) }
    }
    fun cancelReminders() {
        state.value.reminders.forEach { EventReminder.cancel(getApplication(), it) }
        prefs.edit().remove("reminders").apply(); mutable.update { it.copy(reminders = emptySet()) }
    }
    fun reload() {
        if (state.value.loading) return
        viewModelScope.launch {
            mutable.update { it.copy(loading = true, errors = emptyMap(), profile = null, prayers = emptyList(), registrations = emptyList()) }
            suspend fun <T> load(label: String, call: suspend () -> T, apply: (AppState,T) -> AppState) {
                try { val value = call(); mutable.update { apply(it, value) } }
                catch (error: Exception) { if (error is CancellationException) throw error; mutable.update { it.copy(errors = it.errors + (label to (error.message ?: "Falha ao carregar. Tente novamente."))) } }
            }
            if (repo.configured) {
                // Perfil primeiro: renovacao de sessao ocorre antes dos dados restritos.
                if (repo.session != null) load("Conta", { repo.profile() }) { s,v -> s.copy(profile=v) }
                load("Eventos", { repo.events() }) { s,v -> s.copy(events=v) }
                load("Estudos", { repo.studies() }) { s,v -> s.copy(studies=v) }
                load("Galeria", { repo.photos() }) { s,v -> s.copy(photos=v) }
                load("Igreja", { repo.church() }) { s,v -> s.copy(church=v) }
                if (repo.session != null) {
                    load("Aniversariantes", { repo.birthdays() }) { s,v -> s.copy(birthdays=v) }
                    load("Inscrições", { repo.registrations() }) { s,v -> s.copy(registrations=v) }
                    if (ContentAccess.canManage(state.value.profile)) load("Orações", { repo.prayers() }) { s,v -> s.copy(prayers=v) }
                } else mutable.update { it.copy(birthdays = emptyList()) }
            }
            mutable.update { it.copy(loading = false, loggedIn = repo.session != null, email = repo.session?.email.orEmpty(), completed=readSet("studies"), favorites=readSet("favorites"), reminders=prefs.getStringSet("reminders", emptySet())!!.toSet()) }
        }
    }
    fun action(success: String, onSuccess: () -> Unit = {}, block: suspend Repository.() -> Unit) {
        if (state.value.saving) return
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            try { repo.block(); mutable.update { it.copy(message=success) }; onSuccess(); reload() }
            catch (e: Exception) { if (e is CancellationException) throw e; message(e.message ?: "Não foi possível concluir. Verifique sua conexão.") }
            finally { mutable.update { it.copy(saving=false) } }
        }
    }
    fun login(email: String, password: String, done: () -> Unit) = action("Bem-vindo!", done) { login(email,password) }
    fun register(name: String, email: String, password: String, done: () -> Unit) = action("Cadastro recebido. Se a confirmação estiver habilitada, confira seu e-mail.", done) { register(name,email,password) }
    fun logout() = action("Você saiu da conta.") {
        try { logout() } finally {
            mutable.update { it.copy(loggedIn=false, profile=null, email="", birthdays=emptyList(), registrations=emptyList(), prayers=emptyList(), completed=emptySet(), favorites=emptySet()) }
        }
    }
    fun save(table: String, id: String?, body: JSONObject, done: () -> Unit) = action("Cadastro salvo.", done) { saveContent(table,id,body) }
    fun delete(table: String, id: String) = action("Registro excluído.") { deleteContent(table,id) }
    fun loadBooks() {
        if (state.value.books.isNotEmpty() || state.value.bibleLoading) return
        viewModelScope.launch {
            mutable.update { it.copy(bibleLoading=true,bibleError=null) }
            try { val books = repo.books(); mutable.update { it.copy(books=books) } }
            catch(e: Exception) { if(e is CancellationException) throw e; mutable.update { it.copy(bibleError=e.message ?: "Não foi possível consultar os livros.") } }
            finally { mutable.update { it.copy(bibleLoading=false) } }
        }
    }
    fun loadChapter(book: Book, number: Int) {
        val generation = ++chapterGeneration
        viewModelScope.launch {
            mutable.update { it.copy(bibleLoading=true,bibleError=null,chapter=null) }
            try { val chapter = repo.chapter(book,number); if(generation==chapterGeneration) mutable.update { it.copy(chapter=chapter) } }
            catch(e: Exception) { if(e is CancellationException) throw e; if(generation==chapterGeneration) mutable.update { it.copy(bibleError=e.message ?: "Não foi possível carregar o capítulo.") } }
            finally { if(generation==chapterGeneration) mutable.update { it.copy(bibleLoading=false) } }
        }
    }
}
