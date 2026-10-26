package br.org.profetas.app.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.birthday.Birthday
import br.org.profetas.app.church.Church
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.registration.Registration
import br.org.profetas.app.event.weekly.WeeklyService
import br.org.profetas.app.gallery.Photo
import br.org.profetas.app.prayer.Prayer
import br.org.profetas.app.study.Study
import br.org.profetas.app.support.message.UserMessages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

data class AdminState(
    val loading: Boolean = false,
    val hasLoaded: Boolean = false,
    val saving: Boolean = false,
    val failure: String? = null,
    val events: List<Event> = emptyList(),
    val weeklyServices: List<WeeklyService> = emptyList(),
    val studies: List<Study> = emptyList(),
    val photos: List<Photo> = emptyList(),
    val birthdays: List<Birthday> = emptyList(),
    val church: Church = Church(),
    val prayers: List<Prayer> = emptyList(),
    val registrations: List<Registration> = emptyList(),
)

class AdminViewModel(
    private val content: ContentRepositories,
    private val eventPhotos: EventPhotoChange,
    private val galleryPhotos: GalleryPhotoChange,
    private val messages: UserMessages,
    accountId: Flow<String?>,
) : ViewModel() {

    private val mutableState = MutableStateFlow(AdminState())

    val state: StateFlow<AdminState> = mutableState.asStateFlow()

    // Evento de disparo único, não um campo do state: o AdminViewModel sobrevive à navegação, e um "saved = true"
    // preso fecharia sozinho o próximo editor aberto (o defeito que o PrayerViewModel teve).
    private val savedEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val saved: SharedFlow<Unit> = savedEvents.asSharedFlow()

    private var loadJob: Job? = null

    // Este ViewModel vive o processo inteiro, mais que a sessão: sem zerar o estado quando a conta muda, o próximo
    // admin veria as listas do anterior (pedidos de oração e inscrições são privados) até recarregar, já que
    // loadIfEmpty() só olha hasLoaded. Zera em toda troca, não só no logout: pelo link de recuperação a conta muda de
    // A para B sem passar por "deslogado". A carga em curso é cancelada para não repovoar o estado depois do zero.
    init {
        viewModelScope.launch {
            accountId.collect {
                loadJob?.cancel()
                mutableState.value = AdminState()
            }
        }
    }

    fun loadIfEmpty() {
        if (!state.value.hasLoaded) {
            reload()
        }
    }

    fun reload() {
        if (state.value.loading) {
            return
        }

        startLoad()
    }

    fun saveEvent(id: String?, form: EventForm, photo: PhotoIntent) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            eventPhotos.save(id, form.toDraft(ZoneId.systemDefault()), photo)
        }
    }

    fun saveWeeklyService(id: String?, form: WeeklyServiceForm) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            content.weekly.save(id, form.toDraft())
        }
    }

    fun saveStudy(id: String?, form: StudyForm) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            content.studies.save(id, form.toDraft())
        }
    }

    fun savePhoto(current: Photo?, title: String, album: String, photo: PhotoIntent) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            galleryPhotos.save(current, ContentTitle.of(title).value, album.trim(), photo)
        }
    }

    fun saveBirthday(id: String?, form: BirthdayForm) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            content.birthdays.save(id, form.toDraft())
        }
    }

    fun saveChurch(form: ChurchForm) {
        perform(SAVED_MESSAGE, ::emitSaved) {
            content.church.save(form.toChurch())
        }
    }

    fun deleteEvent(event: Event) {
        perform(DELETED_MESSAGE) {
            eventPhotos.delete(event)
        }
    }

    fun deleteWeeklyService(service: WeeklyService) {
        perform(DELETED_MESSAGE) {
            content.weekly.delete(service.id)
        }
    }

    fun deleteStudy(study: Study) {
        perform(DELETED_MESSAGE) {
            content.studies.delete(study.id)
        }
    }

    fun deletePhoto(photo: Photo) {
        perform(DELETED_MESSAGE) {
            galleryPhotos.delete(photo)
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        perform(DELETED_MESSAGE) {
            content.birthdays.delete(birthday.id)
        }
    }

    fun deletePrayer(prayer: Prayer) {
        perform(DELETED_MESSAGE) {
            content.prayers.delete(prayer.id)
        }
    }

    fun cancelRegistration(registration: Registration) {
        perform(DELETED_MESSAGE) {
            content.registrations.deleteAsAdmin(registration.id)
        }
    }

    private fun emitSaved() {
        savedEvents.tryEmit(Unit)
    }

    // Depois de gravar, recarrega antes de avisar: o editor fecha sobre uma lista que já tem a mudança, e o próximo
    // editor aberto parte do registro gravado (com a foto atual), não de uma cópia anterior à gravação.
    private fun perform(success: String, onSuccess: () -> Unit = {}, change: suspend () -> Unit) {
        if (state.value.saving) {
            return
        }

        mutableState.update { current -> current.copy(saving = true) }

        viewModelScope.launch {
            try {
                change()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                mutableState.update { current -> current.copy(saving = false) }
                messages.show(e.message ?: FAILURE_MESSAGE)

                return@launch
            }

            startLoad().join()

            mutableState.update { current -> current.copy(saving = false) }
            messages.show(success)
            onSuccess()
        }
    }

    // Uma carga nova cancela a que estiver em curso: a que começou antes de uma gravação não pode terminar depois
    // dela e devolver a lista velha.
    private fun startLoad(): Job {
        loadJob?.cancel()

        mutableState.update { current -> current.copy(loading = true, failure = null) }

        val job = viewModelScope.launch {
            try {
                val loaded = content.loadAll()

                mutableState.update { current -> loaded.into(current) }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }

                // Mantém as listas anteriores: sem rede, o admin continua vendo o que já carregou.
                val message = e.message ?: "Não foi possível carregar os cadastros."
                mutableState.update { current -> current.copy(loading = false, failure = message) }
            }
        }
        loadJob = job

        return job
    }

    companion object {

        private const val SAVED_MESSAGE = "Cadastro salvo."
        private const val DELETED_MESSAGE = "Registro excluído."
        private const val FAILURE_MESSAGE = "Não foi possível concluir. Verifique sua conexão."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                val content = ContentRepositories(
                    events = container.events,
                    weekly = container.weekly,
                    studies = container.studies,
                    gallery = container.gallery,
                    birthdays = container.birthdays,
                    church = container.church,
                    prayers = container.prayers,
                    registrations = container.registrations,
                )

                AdminViewModel(
                    content = content,
                    eventPhotos = EventPhotoChange(container.imageUploader, container.events),
                    galleryPhotos = GalleryPhotoChange(container.imageUploader, container.gallery),
                    messages = container.userMessages,
                    accountId = container.account.accountId,
                )
            }
        }
    }
}
