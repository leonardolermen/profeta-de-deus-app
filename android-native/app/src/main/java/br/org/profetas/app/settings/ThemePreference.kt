package br.org.profetas.app.settings

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// A chave "dark" é a que o app sempre gravou: trocá-la faria quem já escolheu o tema escuro voltar ao claro.
class ThemePreference(private val preferences: SharedPreferences) {

    private val mutableIsDark = MutableStateFlow(preferences.getBoolean(KEY, false))

    val isDark: StateFlow<Boolean> = mutableIsDark.asStateFlow()

    fun useDark() {
        store(true)
    }

    fun useLight() {
        store(false)
    }

    private fun store(dark: Boolean) {
        preferences.edit().putBoolean(KEY, dark).apply()

        mutableIsDark.value = dark
    }

    private companion object {

        const val KEY = "dark"
    }
}
