package br.org.profetas.app.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhotoTest {

    private val baseUrl = "http://10.0.2.2:54321"

    @Test fun pathWinsOverLegacyUrl() {
        val photo = Photo("1", "Culto", "Cultos", url = "https://old.example/a.jpg", path = "2026/culto 1.jpg")

        assertEquals("$baseUrl/storage/v1/object/public/galeria/2026/culto%201.jpg", photo.imageUrl(baseUrl))
    }

    @Test fun legacyUrlStillWorks() {
        val photo = Photo("1", "Culto", "Cultos", url = "https://old.example/a.jpg", path = null)

        assertEquals("https://old.example/a.jpg", photo.imageUrl(baseUrl))
    }

    @Test fun withoutPathOrUrlThereIsNoImage() {
        val photo = Photo("1", "Culto", "Cultos", url = null, path = null)

        assertNull(photo.imageUrl(baseUrl))
    }
}
