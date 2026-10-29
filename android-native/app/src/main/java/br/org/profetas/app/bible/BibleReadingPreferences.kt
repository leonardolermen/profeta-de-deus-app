package br.org.profetas.app.bible

import android.content.SharedPreferences

class BibleReadingPreferences(private val preferences: SharedPreferences) {
    fun fontSize(): Int = preferences.getInt("bible_font_size", 22).coerceIn(18, 32)
    fun continuous(): Boolean = preferences.getBoolean("bible_continuous_reading", false)
    fun saveFontSize(size: Int) { preferences.edit().putInt("bible_font_size", size.coerceIn(18, 32)).apply() }
    fun saveContinuous(value: Boolean) { preferences.edit().putBoolean("bible_continuous_reading", value).apply() }
}
