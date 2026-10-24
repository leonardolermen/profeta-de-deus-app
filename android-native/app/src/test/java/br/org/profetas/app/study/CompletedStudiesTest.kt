package br.org.profetas.app.study

import br.org.profetas.app.support.storage.InMemoryPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletedStudiesTest {

    @Test fun toggleTwiceRemoves() {
        val completed = CompletedStudies(InMemoryPreferences()) { "visitante" }

        completed.toggle("estudo-1")
        assertTrue("estudo-1" in completed.all())

        completed.toggle("estudo-1")
        assertEquals(emptySet<String>(), completed.all())
    }

    @Test fun keepsExistingKeyFormat() {
        val preferences = InMemoryPreferences(mapOf("studies:conta-1" to setOf("estudo-1")))

        val completed = CompletedStudies(preferences) { "conta-1" }

        assertEquals(setOf("estudo-1"), completed.all())
    }
}
