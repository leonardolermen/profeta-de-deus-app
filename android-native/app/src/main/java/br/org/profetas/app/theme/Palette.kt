package br.org.profetas.app.theme

import androidx.compose.ui.graphics.Color

// Valores exatos das Global Constraints do projeto (paleta clara da Igreja Profetas de Deus).
object Palette {

    val Primary = Color(0xFF0056B3)
    val Accent = Color(0xFF00A8E8)
    val Text = Color(0xFF0B132B)
    val Surface = Color(0xFFF4F7FA)
    val Border = Color(0xFFE1E9F0)
    val Muted = Color(0xFF6B7A90)
    val Background = Color(0xFFFFFFFF)
    val OnPhoto = Color(0xFFFFFFFF)
    val DayBadge = Color(0xFFEBF4FA)

    // Escuras: o texto claro vira o fundo escuro e a superfície clara vira o texto escuro — mesma
    // paleta, papéis invertidos — mantendo Primary/Accent reconhecíveis (mais claros para contraste).
    val DarkPrimary = Color(0xFF4C9FFF)
    val DarkAccent = Color(0xFF33C3FF)
    val DarkText = Color(0xFFF4F7FA)
    val DarkSurface = Color(0xFF16213A)
    val DarkBorder = Color(0xFF2A3A54)
    val DarkMuted = Color(0xFF8291A8)
    val DarkBackground = Color(0xFF0B132B)
    val DarkOnPhoto = Color(0xFFFFFFFF)
    val DarkDayBadge = Color(0xFF1C2D42)
}
