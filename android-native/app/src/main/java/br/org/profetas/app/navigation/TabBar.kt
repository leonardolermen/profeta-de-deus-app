package br.org.profetas.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private class TabItem(val route: Route, val label: String, val icon: ImageVector)

// Mesmos ícones do Expo (house.fill, book.fill, calendar, gear → MaterialIcons em components/ui/IconSymbol.tsx).
private val tabItems = listOf(
    TabItem(Route.Home, "Início", Icons.Filled.Home),
    TabItem(Route.Bible, "Bíblia", Icons.AutoMirrored.Filled.MenuBook),
    TabItem(Route.Events, "Eventos", Icons.Filled.Event),
    TabItem(Route.Settings, "Ajustes", Icons.Filled.Settings),
)

// Barra de abas de app/(tabs)/_layout.tsx: fundo do tema, borda superior e só a cor marcando a aba ativa
// (sem a pílula de seleção do Material 3).
@Composable
fun TabBar(selected: Route, onSelect: (Route) -> Unit) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent,
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
            tabItems.forEach { item ->
                NavigationBarItem(
                    selected = item.route == selected,
                    onClick = { onSelect(item.route) },
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    colors = colors,
                )
            }
        }
    }
}
