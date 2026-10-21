package br.org.profetas.app

import br.org.profetas.app.data.ContentAccess
import br.org.profetas.app.data.Profile
import org.junit.Assert.*
import org.junit.Test

class ContentAccessTest {
    @Test fun visitorNeverGetsContentPermissions() {
        assertFalse(ContentAccess.canManage(null))
        assertTrue(runCatching { ContentAccess.requireAdmin(null) }.exceptionOrNull() is SecurityException)
    }
    @Test fun memberAndUnknownRolesCannotManageContent() {
        listOf("membro", "", "ADMIN", "owner").forEach { role ->
            assertFalse(ContentAccess.canManage(Profile("user", "Pessoa", role)))
        }
    }
    @Test fun administratorCanManageContent() {
        val admin = Profile("admin-id", "Administrador", "admin")
        assertTrue(ContentAccess.canManage(admin))
        ContentAccess.requireAdmin(admin)
    }
    @Test(expected = SecurityException::class) fun writeGuardRejectsMember() {
        ContentAccess.requireAdmin(Profile("user", "Membro", "membro"))
    }
    @Test(expected = SecurityException::class) fun revokedRoleLosesWriteAccess() {
        val previous = Profile("same-user", "Pessoa", "admin")
        ContentAccess.requireAdmin(previous)
        ContentAccess.requireAdmin(previous.copy(role = "membro"))
    }
}
