package br.org.profetas.app

import android.content.Context
import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.bible.BibleRepository
import br.org.profetas.app.birthday.BirthdayRepository
import br.org.profetas.app.church.ChurchRepository
import br.org.profetas.app.event.EventRepository
import br.org.profetas.app.event.registration.RegistrationRepository
import br.org.profetas.app.gallery.GalleryRepository
import br.org.profetas.app.prayer.PrayerRepository
import br.org.profetas.app.study.StudyRepository
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.message.UserMessages
import br.org.profetas.app.support.session.KeystoreSessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Injeção de dependência manual: um SupabaseClient (e uma sessão) para o app inteiro. */
class AppContainer(context: Context) {

    private val config = SupabaseConfig.fromBuildConfig()

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val client = SupabaseClient(config, KeystoreSessionStorage(context), http)

    // Vive o processo inteiro, como o próprio container; só mantém os estados derivados da sessão.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val account = AccountRepository(client, applicationScope)

    private val guard = ContentWriteGuard(account)

    val events = EventRepository(client, guard)
    val registrations = RegistrationRepository(client, guard)
    val studies = StudyRepository(client, guard)
    val prayers = PrayerRepository(client, guard)
    val birthdays = BirthdayRepository(client, guard)
    val gallery = GalleryRepository(client, guard)
    val church = ChurchRepository(client, guard)
    val bible = BibleRepository(client)

    val userMessages = UserMessages()

    val isConfigured: Boolean get() = client.isConfigured

    // Base das URLs públicas do Storage (fotos guardadas por caminho no bucket).
    val storageBaseUrl: String get() = config.url
}
