package br.org.profetas.app.admin

import br.org.profetas.app.birthday.Birthday
import br.org.profetas.app.birthday.BirthdayRepository
import br.org.profetas.app.church.Church
import br.org.profetas.app.church.ChurchRepository
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventRepository
import br.org.profetas.app.event.registration.Registration
import br.org.profetas.app.event.registration.RegistrationRepository
import br.org.profetas.app.event.weekly.WeeklyService
import br.org.profetas.app.event.weekly.WeeklyServiceRepository
import br.org.profetas.app.gallery.GalleryRepository
import br.org.profetas.app.gallery.Photo
import br.org.profetas.app.prayer.Prayer
import br.org.profetas.app.prayer.PrayerRepository
import br.org.profetas.app.study.Study
import br.org.profetas.app.study.StudyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// Os repositórios de conteúdo que a Administração lê e edita, juntos para o AdminViewModel não receber oito.
class ContentRepositories(
    val events: EventRepository,
    val weekly: WeeklyServiceRepository,
    val studies: StudyRepository,
    val gallery: GalleryRepository,
    val birthdays: BirthdayRepository,
    val church: ChurchRepository,
    val prayers: PrayerRepository,
    val registrations: RegistrationRepository,
) {

    // As oito leituras em paralelo: a Administração só aparece inteira, e uma falha em qualquer uma vira o aviso de
    // carga (mantendo as listas anteriores) em vez de uma seção vazia que pareceria "nenhum cadastro".
    suspend fun loadAll(): AdminContent = coroutineScope {
        val loadedEvents = async { events.all() }
        val loadedWeekly = async { weekly.all() }
        val loadedStudies = async { studies.all() }
        val loadedPhotos = async { gallery.all() }
        val loadedBirthdays = async { birthdays.all() }
        val loadedChurch = async { church.details() }
        val loadedPrayers = async { prayers.all() }
        val loadedRegistrations = async { registrations.all() }

        AdminContent(
            events = loadedEvents.await(),
            weeklyServices = loadedWeekly.await(),
            studies = loadedStudies.await(),
            photos = loadedPhotos.await(),
            birthdays = loadedBirthdays.await(),
            church = loadedChurch.await(),
            prayers = loadedPrayers.await(),
            registrations = loadedRegistrations.await(),
        )
    }
}

class AdminContent(
    val events: List<Event>,
    val weeklyServices: List<WeeklyService>,
    val studies: List<Study>,
    val photos: List<Photo>,
    val birthdays: List<Birthday>,
    val church: Church,
    val prayers: List<Prayer>,
    val registrations: List<Registration>,
) {

    fun into(state: AdminState): AdminState = state.copy(
        loading = false,
        hasLoaded = true,
        events = events,
        weeklyServices = weeklyServices,
        studies = studies,
        photos = photos,
        birthdays = birthdays,
        church = church,
        prayers = prayers,
        registrations = registrations,
    )
}
