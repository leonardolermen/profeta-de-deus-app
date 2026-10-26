package br.org.profetas.app.settings

import br.org.profetas.app.support.storage.InMemoryPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePreferenceTest {

    @Test fun readsExistingKey() {
        val preferences = InMemoryPreferences(mapOf("dark" to true))

        val theme = ThemePreference(preferences)

        assertTrue(theme.isDark.value)
    }

    @Test fun startsLightWithoutStoredChoice() {
        val theme = ThemePreference(InMemoryPreferences())

        assertFalse(theme.isDark.value)
    }

    @Test fun useLightStoresFalse() {
        val preferences = InMemoryPreferences(mapOf("dark" to true))
        val theme = ThemePreference(preferences)

        theme.useLight()

        assertFalse(theme.isDark.value)
        assertFalse(preferences.getBoolean("dark", true))
    }

    @Test fun useDarkStoresTrue() {
        val preferences = InMemoryPreferences()
        val theme = ThemePreference(preferences)

        theme.useDark()

        assertTrue(theme.isDark.value)
        assertTrue(preferences.getBoolean("dark", false))
    }
}
