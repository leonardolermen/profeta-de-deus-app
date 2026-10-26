package br.org.profetas.app.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Peças comuns de login, cadastro e recuperação: no Expo (app/auth/*.tsx) as duas telas têm o mesmo
// scrollContent, header, botão e rodapé, mudando só o texto.

// scrollContent de app/auth/login.tsx.
@Composable
fun AuthPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 40.dp),
        content = content,
    )
}

// header, iconContainer, title e subtitle de app/auth/login.tsx.
@Composable
fun AuthHeader(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 40.sp)
        }

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Text(
            text = subtitle,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

// btnSubmit e gradientBtn: gradiente horizontal accent -> primary, sombra azul e 70% de opacidade enquanto envia.
// Durante o envio o indicador toma o lugar do texto, como o ActivityIndicator do Expo.
@Composable
fun GradientSubmitButton(label: String, isSending: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(if (isSending) 0.7f else 1f)
            .shadow(elevation = 6.dp, shape = shape, ambientColor = colors.primary, spotColor = colors.primary)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(colors.secondary, colors.primary)))
            .clickable(enabled = !isSending, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSending) {
            CircularProgressIndicator(color = colors.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.onPrimary)
        }
    }
}

// footer, footerText e footerLink de app/auth/login.tsx.
@Composable
fun AuthFooter(text: String, link: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text(
            text = link,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        )
    }
}
