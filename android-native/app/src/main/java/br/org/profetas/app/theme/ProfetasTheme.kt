package br.org.profetas.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun ProfetasTheme(isDark: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = Palette.DarkPrimary,
            secondary = Palette.DarkAccent,
            background = Palette.DarkBackground,
            surface = Palette.DarkSurface,
            onBackground = Palette.DarkText,
            onSurface = Palette.DarkText,
            onSurfaceVariant = Palette.DarkMuted,
            outline = Palette.DarkBorder,
        )
    } else {
        lightColorScheme(
            primary = Palette.Primary,
            secondary = Palette.Accent,
            background = Palette.Background,
            surface = Palette.Surface,
            onBackground = Palette.Text,
            onSurface = Palette.Text,
            onSurfaceVariant = Palette.Muted,
            outline = Palette.Border,
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
