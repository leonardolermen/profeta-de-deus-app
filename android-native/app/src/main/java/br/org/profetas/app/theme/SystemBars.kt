package br.org.profetas.app.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// O enableEdgeToEdge() da Activity decide o estilo das barras pelo modo escuro do sistema, mas o tema escuro do app
// é uma preferência própria: com o sistema claro e o app escuro, o relógio e os ícones ficavam escuros sobre fundo
// escuro e sumiam.
fun usesLightBarIcons(isDark: Boolean): Boolean = isDark

@Composable
fun SystemBarIcons(isDark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) {
        return
    }

    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        // No AndroidX "appearance light" significa ícones escuros, o oposto de usesLightBarIcons.
        controller.isAppearanceLightStatusBars = !usesLightBarIcons(isDark)
        controller.isAppearanceLightNavigationBars = !usesLightBarIcons(isDark)
    }
}
