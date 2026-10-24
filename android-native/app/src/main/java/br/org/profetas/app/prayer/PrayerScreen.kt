package br.org.profetas.app.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.ui.AppTextArea
import br.org.profetas.app.support.ui.AppTextField

// Oração, a partir de app/oracao.tsx: as medidas são as do StyleSheet de lá. O contador de caracteres e as
// faixas de tamanho (nome 2..120, pedido 5..4000) vêm da tela Kotlin anterior, sem equivalente no Expo.
@Composable
fun PrayerScreen(viewModel: PrayerViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }

    // O Expo fecha a tela com um Alerta ao confirmar; aqui a mensagem já sai pelo snackbar, então o pedido
    // enviado com sucesso volta sozinho para quem chamou a tela (o atalho "Oração" da Início).
    LaunchedEffect(state.sent) {
        if (state.sent) {
            onBack()
        }
    }

    val isValid = name.trim().length in MIN_NAME_LENGTH..MAX_NAME_LENGTH &&
        text.trim().length in MIN_TEXT_LENGTH..MAX_TEXT_LENGTH

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
    ) {
        Header()

        AppTextField(
            label = "Seu Nome",
            value = name,
            onChange = { input -> name = input.take(MAX_NAME_LENGTH) },
            placeholder = "Ex: João da Silva",
        )

        AppTextArea(
            label = "Seu Pedido",
            value = text,
            onChange = { input -> text = input.take(MAX_TEXT_LENGTH) },
            placeholder = "Descreva aqui o seu pedido de oração. Deus está no controle de tudo...",
            modifier = Modifier.padding(top = 20.dp),
        )

        Text(
            text = "${text.length}/$MAX_TEXT_LENGTH",
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )

        SubmitButton(
            enabled = !state.sending && isValid,
            isSending = state.sending,
            onClick = { viewModel.send(name, text) },
            modifier = Modifier.padding(top = 26.dp),
        )
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🙏", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))

        Text(
            text = "Como podemos orar por você?",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        Text(
            text = "Deixe seu pedido. Nossa equipe pastoral e intercessores estarão clamando ao Senhor pela " +
                "sua vida!",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

// btnSubmit/btnText de app/oracao.tsx: altura, raio e sombra azul próprios, maiores que o BlockButton padrão.
// Enquanto envia, mostra o indicador no lugar do texto, como o ActivityIndicator do Expo.
@Composable
private fun SubmitButton(enabled: Boolean, isSending: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(if (enabled || isSending) 1f else 0.5f)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSending) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                text = "Enviar Pedido de Oração",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
