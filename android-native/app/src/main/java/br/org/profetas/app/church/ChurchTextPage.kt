package br.org.profetas.app.church

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.R
import br.org.profetas.app.support.ui.LoadFailure

// Página de texto corrido da igreja, a partir de app/historia.tsx: logo, título e parágrafo. História e Privacidade
// mudam só o título e o texto; o aviso de texto vazio (quando a igreja ainda não publicou) é de cada uma.
@Composable
fun ChurchTextPage(state: ChurchState, title: String, text: String, whenBlank: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 10.dp),
        )

        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
            )

            state.failure?.let { failure ->
                LoadFailure(message = failure, onRetry = onRetry, modifier = Modifier.padding(bottom = 16.dp))
            }

            when {
                text.isNotBlank() -> PageText(text)

                state.loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

                // Com falha, o cartão acima já explica; dizer "não publicado" aqui esconderia que o problema é a rede.
                state.failure == null -> PageText(whenBlank)
            }
        }
    }
}

@Composable
private fun PageText(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
