package br.org.profetas.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class TabItem(val route: Route, val label: String, val icon: ImageVector)

// Mesmos ícones do Expo (house.fill, book.fill, calendar, gear → MaterialIcons em components/ui/IconSymbol.tsx).
private val tabItems = listOf(
    TabItem(Route.Home, "Início", Icons.Filled.Home),
    TabItem(Route.Bible, "Bíblia", Icons.AutoMirrored.Filled.MenuBook),
    TabItem(Route.Events, "Eventos", Icons.Filled.Event),
    TabItem(Route.Settings, "Ajustes", Icons.Filled.Settings),
)

// Barra de abas de app/(tabs)/_layout.tsx: 49dp como a do React Navigation, ícone 28dp, só a cor marcando a aba
// ativa. Não é a NavigationBar do Material 3: ela tem 80dp e a pílula de seleção, que o Expo não tem.
@Composable
fun TabBar(selected: Route, onSelect: (Route) -> Unit) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp)
                .selectableGroup(),
        ) {
            tabItems.forEach { item ->
                TabButton(
                    item = item,
                    isSelected = item.route == selected,
                    onClick = { onSelect(item.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TabButton(item: TabItem, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(item.icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))

        Text(text = item.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color, lineHeight = 13.sp)
    }
}
