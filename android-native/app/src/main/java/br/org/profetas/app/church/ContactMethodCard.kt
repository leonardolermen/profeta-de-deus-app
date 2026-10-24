package br.org.profetas.app.church

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.theme.Palette

enum class ContactCardStyle {
    Highlight,
    Plain,
}

// Cartão de forma de contato de app/contato.tsx (methodCard). O destaque é o cartão cheio do WhatsApp, em verde com
// texto branco; os demais são a superfície com borda.
@Composable
fun ContactMethodCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ContactCardStyle = ContactCardStyle.Plain,
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = style.colors()
    val border = if (colors.bordered) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape) else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.container)
            .then(border)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, fontSize = 28.sp, modifier = Modifier.padding(end = 16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.title)

            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.description,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Text(text = "→", fontSize = 24.sp, color = colors.arrow)
    }
}

private class ContactCardColors(
    val container: Color,
    val title: Color,
    val description: Color,
    val arrow: Color,
    val bordered: Boolean,
)

@Composable
private fun ContactCardStyle.colors(): ContactCardColors = when (this) {
    ContactCardStyle.Highlight -> ContactCardColors(
        container = Palette.WhatsApp,
        title = Palette.OnPhoto,
        description = Palette.OnPhoto.copy(alpha = 0.9f),
        arrow = Palette.OnPhoto,
        bordered = false,
    )

    ContactCardStyle.Plain -> ContactCardColors(
        container = MaterialTheme.colorScheme.surface,
        title = MaterialTheme.colorScheme.onBackground,
        description = MaterialTheme.colorScheme.onSurfaceVariant,
        arrow = MaterialTheme.colorScheme.primary,
        bordered = true,
    )
}
