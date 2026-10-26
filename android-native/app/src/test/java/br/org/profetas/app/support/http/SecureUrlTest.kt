package br.org.profetas.app.support.http

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureUrlTest {

    @Test fun secureUrlRequiresAnActualHttpsHost() {
        assertTrue(isSecureUrl("https://example.org/photo.jpg"))
        assertFalse(isSecureUrl("http://example.org/photo.jpg"))
        assertFalse(isSecureUrl("https:///photo.jpg"))
        assertFalse(isSecureUrl("javascript:alert(1)"))
        assertFalse(isSecureUrl("https://user:password@example.org/image.jpg"))
    }
}
