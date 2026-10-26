package br.org.profetas.app.support.storage

import android.content.SharedPreferences

// SharedPreferences em memória para os testes de JVM, onde o Android não existe. Só guarda; não avisa ouvintes.
class InMemoryPreferences(initial: Map<String, Any?> = emptyMap()) : SharedPreferences {

    private val values = initial.toMutableMap()

    override fun getAll(): Map<String, *> = values.toMap()

    override fun getString(key: String, defaultValue: String?): String? = values[key] as String? ?: defaultValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defaultValues: Set<String>?): Set<String>? =
        values[key] as Set<String>? ?: defaultValues

    override fun getInt(key: String, defaultValue: Int): Int = values[key] as Int? ?: defaultValue

    override fun getLong(key: String, defaultValue: Long): Long = values[key] as Long? ?: defaultValue

    override fun getFloat(key: String, defaultValue: Float): Float = values[key] as Float? ?: defaultValue

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = values[key] as Boolean? ?: defaultValue

    override fun contains(key: String): Boolean = key in values

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
    }

    private inner class Editor : SharedPreferences.Editor {

        private val pending = mutableMapOf<String, Any?>()

        private var clearsFirst = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = put(key, value)

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor =
            put(key, values?.toSet())

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = put(key, value)

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = put(key, value)

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = put(key, value)

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = put(key, value)

        override fun remove(key: String): SharedPreferences.Editor = put(key, null)

        override fun clear(): SharedPreferences.Editor {
            clearsFirst = true

            return this
        }

        override fun commit(): Boolean {
            apply()

            return true
        }

        override fun apply() {
            if (clearsFirst) {
                values.clear()
            }

            pending.forEach { (key, value) ->
                if (value == null) {
                    values.remove(key)
                } else {
                    values[key] = value
                }
            }
        }

        private fun put(key: String, value: Any?): SharedPreferences.Editor {
            pending[key] = value

            return this
        }
    }
}
