package br.org.profetas.app.admin

import br.org.profetas.app.account.AccountRepository
import br.org.profetas.app.account.ContentWriteGuard
import br.org.profetas.app.birthday.BirthdayRepository
import br.org.profetas.app.church.ChurchRepository
import br.org.profetas.app.event.EventRepository
import br.org.profetas.app.event.registration.RegistrationRepository
import br.org.profetas.app.event.weekly.WeeklyServiceRepository
import br.org.profetas.app.gallery.GalleryRepository
import br.org.profetas.app.prayer.PrayerRepository
import br.org.profetas.app.study.StudyRepository
import br.org.profetas.app.support.http.SupabaseClient
import br.org.profetas.app.support.http.SupabaseConfig
import br.org.profetas.app.support.message.UserMessages
import br.org.profetas.app.support.session.Session
import br.org.profetas.app.support.session.SessionStorage
import br.org.profetas.app.support.storage.PhotoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private class VisitorSessionStorage : SessionStorage {

        override fun load(): Session? = Session("A", "R", "admin-a", "admin@local.test")

        override fun save(session: Session) {
        }

        override fun clear() {
        }
    }

    private val server = MockWebServer()

    private val prayerReads = AtomicInteger()

    @Before fun startServer() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                if (path.contains("perfis")) return MockResponse().setBody("""[{"id":"admin-a","nome":"Admin","role":"admin"}]""")

                if (!path.contains("oracoes")) {
                    return MockResponse().setResponseCode(200).setBody("[]")
                }

                prayerReads.incrementAndGet()

                return MockResponse().setResponseCode(200).setBody(PRAYER_ROWS)
            }
        }
        server.start()
    }

    @After fun stopServer() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    // Os repositórios de verdade contra um MockWebServer, como nos outros ViewModels.
    private fun TestScope.viewModel(scope: CoroutineScope, accountId: MutableStateFlow<String?>): AdminViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val config = SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true)
        val client = SupabaseClient(config, VisitorSessionStorage(), OkHttpClient())
        val guard = ContentWriteGuard(AccountRepository(client, scope))
        val gallery = GalleryRepository(client, guard)
        val events = EventRepository(client, guard)

        val content = ContentRepositories(
            events = events,
            weekly = WeeklyServiceRepository(client, guard),
            studies = StudyRepository(client, guard),
            gallery = gallery,
            birthdays = BirthdayRepository(client, guard),
            church = ChurchRepository(client, guard),
            prayers = PrayerRepository(client, guard),
            registrations = RegistrationRepository(client, guard),
        )

        return AdminViewModel(
            content = content,
            eventPhotos = EventPhotoChange(mock(PhotoStorage::class.java), events),
            galleryPhotos = GalleryPhotoChange(mock(PhotoStorage::class.java), gallery),
            messages = UserMessages(),
            accountId = accountId,
        )
    }

    // A rede do MockWebServer roda em threads reais: o tempo virtual do runTest não espera por ela.
    private suspend fun AdminViewModel.awaitLoaded() {
        withContext(Dispatchers.Default) {
            withTimeout(LOAD_TIMEOUT_MILLIS) {
                state.first { current -> current.hasLoaded }
            }
        }
    }

    // Dois admins podem entrar em sequência no mesmo processo: as listas do primeiro (inclusive pedidos de oração e
    // inscrições, privados) não podem aparecer para o segundo enquanto a carga dele não termina.
    @Test fun sessionEndClearsLoadedContentSoNextAdminReloads() = runTest {
        val accountId = MutableStateFlow<String?>("admin-a")
        val viewModel = viewModel(backgroundScope, accountId)

        viewModel.loadIfEmpty()
        viewModel.awaitLoaded()

        assertTrue(viewModel.state.value.hasLoaded)
        assertEquals(1, viewModel.state.value.prayers.size)

        accountId.value = null

        assertFalse(viewModel.state.value.hasLoaded)
        assertEquals(emptyList<Any>(), viewModel.state.value.prayers)

        accountId.value = "admin-b"
        viewModel.loadIfEmpty()
        viewModel.awaitLoaded()

        assertEquals(2, prayerReads.get())
        assertEquals(1, viewModel.state.value.prayers.size)
    }

    // A troca de conta pelo link de recuperação não passa por "deslogado": o reset não pode depender disso.
    @Test fun accountSwitchWithoutLogoutClearsLoadedContent() = runTest {
        val accountId = MutableStateFlow<String?>("admin-a")
        val viewModel = viewModel(backgroundScope, accountId)

        viewModel.loadIfEmpty()
        viewModel.awaitLoaded()

        accountId.value = "admin-b"

        assertFalse(viewModel.state.value.hasLoaded)
        assertEquals(emptyList<Any>(), viewModel.state.value.prayers)
    }

    private companion object {

        const val LOAD_TIMEOUT_MILLIS = 5_000L

        const val PRAYER_ROWS = """[{"id":"1","nome":"Ana","pedido":"Orem por mim","criado_em":"2026-10-01"}]"""
    }
}
