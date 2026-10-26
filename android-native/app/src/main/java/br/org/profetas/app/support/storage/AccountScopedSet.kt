package br.org.profetas.app.support.storage

import android.content.SharedPreferences

// Conjunto de textos guardado no aparelho, um por conta: chave "<tipo>:<conta>". É o formato que o app já gravava
// ("favorites:visitante", "studies:<id do usuário>"); mudar a chave faria quem já usa o app perder o que marcou.
class AccountScopedSet(
    private val preferences: SharedPreferences,
    private val type: String,
    private val accountId: () -> String,
) {

    fun all(): Set<String> = preferences.getStringSet(key(), emptySet()).orEmpty().toSet()

    fun toggle(value: String) {
        val values = all().toMutableSet()

        val wasAbsent = values.add(value)
        if (!wasAbsent) {
            values.remove(value)
        }

        preferences.edit().putStringSet(key(), values).apply()
    }

    private fun key(): String = "$type:${accountId()}"
}
