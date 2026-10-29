package br.org.profetas.app.prayer

import br.org.profetas.app.account.*
import br.org.profetas.app.support.http.*
import br.org.profetas.app.support.message.UserMessages
import br.org.profetas.app.support.session.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReceivedPrayersTest {
    private val server = MockWebServer()
    private val profiles = MutableStateFlow<Profile?>(null)
    private var serverRole = "admin"
    private var reads = 0

    @Before fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.path.orEmpty().contains("perfis")) return MockResponse().setBody("""[{"id":"admin","nome":"Ana","role":"$serverRole"}]""")
                reads++
                return MockResponse().setBody("""[{"id":"1","nome":"Maria","pedido":"Minha família","criado_em":"2026-10-29"}]""")
            }
        }
        server.start()
    }

    @After fun finish() { Dispatchers.resetMain(); server.shutdown() }

    private fun TestScope.model(): PrayerViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val storage = object : SessionStorage {
            override fun load() = Session("A", "R", "admin", "admin@local.test")
            override fun save(session: Session) {}
            override fun clear() {}
        }
        val client = SupabaseClient(SupabaseConfig("http://127.0.0.1:${server.port}", "anon-key", allowLocalHttp = true), storage, OkHttpClient())
        return PrayerViewModel(PrayerRepository(client, ContentWriteGuard(AccountRepository(client, backgroundScope))), UserMessages(), profiles)
    }

    private suspend fun PrayerViewModel.awaitResult() = withContext(Dispatchers.Default) {
        withTimeout(5_000) { state.first { !it.loading } }
    }

    @Test fun memberCannotRequestPrivateList() = runTest {
        profiles.value = Profile("member", "Membro", "membro")
        val vm = model()
        vm.loadReceived()
        assertEquals(0, server.requestCount)
        assertFalse(vm.state.value.canManage)
    }

    @Test fun logoutClearsReceivedPrayers() = runTest {
        profiles.value = Profile("admin", "Ana", "admin")
        val vm = model()
        vm.loadReceived()
        vm.awaitResult()
        assertEquals("Maria", vm.state.value.received.single().name)
        profiles.value = null
        assertTrue(vm.state.value.received.isEmpty())
        assertFalse(vm.state.value.canManage)
    }

    @Test fun revokedRoleBlocksReadEvenWithOldAdminProfile() = runTest {
        profiles.value = Profile("admin", "Ana", "admin")
        serverRole = "membro"
        val vm = model()
        vm.loadReceived()
        vm.awaitResult()
        assertEquals(0, reads)
        assertTrue(vm.state.value.received.isEmpty())
        assertNotNull(vm.state.value.failure)
    }

    @Test fun changingAdminClearsPreviousPrivateList() = runTest {
        profiles.value = Profile("admin", "Ana", "admin")
        val vm = model()
        vm.loadReceived()
        vm.awaitResult()
        profiles.value = Profile("other", "Outro", "admin")
        assertTrue(vm.state.value.received.isEmpty())
    }
}
