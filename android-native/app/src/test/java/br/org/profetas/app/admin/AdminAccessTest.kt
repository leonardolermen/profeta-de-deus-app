package br.org.profetas.app.admin

import br.org.profetas.app.account.Profile
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminAccessTest {

    private val admin = Profile(id = "admin-id", name = "Admin", role = "admin")

    private val member = Profile(id = "member-id", name = "Maria", role = "member")

    @Test fun adminProfileIsGrantedEvenWithAnOldFailure() {
        assertEquals(AdminAccess.Granted, AdminAccess.of(admin, "admin-id", profileFailure = "Sem conexão"))
    }

    @Test fun loggedOutIsDenied() {
        assertEquals(AdminAccess.Denied, AdminAccess.of(profile = null, accountId = null, profileFailure = null))
    }

    @Test fun memberIsDenied() {
        assertEquals(AdminAccess.Denied, AdminAccess.of(member, "member-id", profileFailure = null))
    }

    @Test fun loggedInWithoutProfileIsLoading() {
        assertEquals(AdminAccess.Loading, AdminAccess.of(profile = null, "admin-id", profileFailure = null))
    }

    // Antes, a falha ao carregar o perfil deixava a administração num spinner sem fim.
    @Test fun failedProfileLoadOffersRetry() {
        val access = AdminAccess.of(profile = null, "admin-id", profileFailure = "Sem conexão")

        assertEquals(AdminAccess.Failed("Sem conexão"), access)
    }
}
