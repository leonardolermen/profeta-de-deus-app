package br.org.profetas.app.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBarsTest {

    @Test
    fun darkThemeUsesLightBarIcons() {
        assertTrue(usesLightBarIcons(isDark = true))
    }

    @Test
    fun lightThemeUsesDarkBarIcons() {
        assertFalse(usesLightBarIcons(isDark = false))
    }
}
