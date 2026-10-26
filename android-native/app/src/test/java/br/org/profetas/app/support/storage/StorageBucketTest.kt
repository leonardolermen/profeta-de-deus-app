package br.org.profetas.app.support.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class StorageBucketTest {

    @Test fun buildsPublicUrl() {
        val url = StorageBucket.GALLERY.publicUrl("http://10.0.2.2:54321/", "2026/a b.jpg")

        assertEquals("http://10.0.2.2:54321/storage/v1/object/public/galeria/2026/a%20b.jpg", url)
    }
}
