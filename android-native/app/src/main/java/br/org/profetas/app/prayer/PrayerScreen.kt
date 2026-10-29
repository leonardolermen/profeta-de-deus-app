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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.support.ui.LoadFailure
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
    var showingReceived by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.canManage, showingReceived) {
        if (state.canManage && showingReceived) viewModel.loadReceived()
        if (!state.canManage) showingReceived = false
    }

    // O Expo fecha a tela com um Alerta ao confirmar; aqui a mensagem já sai pelo snackbar, então o pedido
    // enviado com sucesso volta sozinho para quem chamou a tela (o atalho "Oração" da Início). `sent` é um
    // evento de disparo único (não um campo do state): assim uma nova visita à tela, depois de um envio
    // anterior bem-sucedido, não é mandada de volta sozinha de novo.
    LaunchedEffect(viewModel) {
        viewModel.sent.collect {
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
        if (state.canManage) {
            Text("Orações", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = !showingReceived, onClick = { showingReceived = false }, label = { Text("Pedir oração") }, modifier = Modifier.heightIn(min = 48.dp))
                FilterChip(selected = showingReceived, onClick = { showingReceived = true }, label = { Text("Pedidos recebidos") }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }

        if (state.canManage && showingReceived) {
            Text("Pedidos à equipe pastoral", fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Text("Visível somente para administradores.", fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp))
            OutlinedButton(onClick = viewModel::loadReceived, enabled = !state.loading, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Atualizar pedidos", fontSize = 18.sp) }
            if (state.loading) CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            state.failure?.let { LoadFailure(it, viewModel::loadReceived) }
            if (!state.loading && state.failure == null && state.received.isEmpty()) Text("Nenhum pedido recebido.", modifier = Modifier.padding(vertical = 20.dp), fontSize = 18.sp)
            state.received.forEach { prayer ->
                OutlinedCard(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(prayer.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(displayDate(prayer.date), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(prayer.text, fontSize = 20.sp, lineHeight = 30.sp, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        } else {
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
