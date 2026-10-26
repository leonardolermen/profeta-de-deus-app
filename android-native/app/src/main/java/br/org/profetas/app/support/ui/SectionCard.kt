package br.org.profetas.app.support.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Cartão de seção (sectionCard de app/(tabs)/index.tsx). A margem externa do cartão fica a cargo de quem o
// posiciona, então o cartão só cuida do próprio padding. Título e sublinhado são peças à parte porque no Expo
// nem todo cartão tem sublinhado ("Onde nos encontrar" não tem).
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        content = content,
    )
}

// sectionTitle de app/(tabs)/index.tsx.
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

// underline de app/(tabs)/index.tsx, mas em primary a 12%: o marrom do Expo era sobra de template e quase não
// aparecia (decisão do controller na Task 5).
@Composable
fun SectionUnderline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .size(width = 60.dp, height = 3.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
    )
}
