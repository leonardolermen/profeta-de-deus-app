package br.org.profetas.app.baptism

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.R
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventsViewModel
import br.org.profetas.app.event.dayMonthAndTime
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.BorderedCard

private val preparationSteps = listOf(
    "Aceitar Jesus como seu único Senhor e Salvador genuinamente.",
    "Participar do curso de preparação para novos convertidos (4 aulas).",
    "Preencher sua ficha de cadastro com a secretaria da igreja.",
)

// Batismo, a partir de app/batismo.tsx: as medidas são as do StyleSheet de lá. O "Quero me batizar" é novo e leva
// à inscrição no detalhe do próximo batismo.
@Composable
fun BaptismScreen(viewModel: EventsViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val nextBaptism = viewModel.nextBaptism()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.batismo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )

        Header()

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeading("O que é o batismo?")

            Text(
                text = "O batismo nas águas é uma ordenança de Jesus Cristo e um passo fundamental de obediência " +
                    "para todo aquele que decide segui-Lo. Ele representa publicamente a nossa morte para o " +
                    "pecado e a nossa ressurreição para uma nova vida com Deus.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            BorderedCard(modifier = Modifier.padding(bottom = 32.dp)) {
                NextBaptism(
                    baptism = nextBaptism,
                    isLoading = state.loading,
                    onEnroll = { baptism -> onNavigate(Route.EventDetail(baptism.id)) },
                )

                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )

                InfoRow(icon = "📍", label = "Local") {
                    InfoValue("Associação Recanto Silvestre")
                }
            }

            SectionHeading("Como me preparar?")

            preparationSteps.forEachIndexed { index, step ->
                Step(number = index + 1, text = step)
            }

            ContactButton(onClick = { onNavigate(Route.Contact) }, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)) {
        Text(
            text = "Nascer de Novo",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "\"Quem crer e for batizado será salvo.\" - Marcos 16:16",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NextBaptism(baptism: Event?, isLoading: Boolean, onEnroll: (Event) -> Unit) {
    InfoRow(icon = "🗓", label = "Próximas Águas") {
        when {
            baptism != null -> {
                InfoValue(dayMonthAndTime(baptism.date))

                Text(
                    text = baptism.title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            isLoading -> CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(20.dp),
            )

            else -> Text(
                text = "Nenhuma data agendada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    baptism?.let { found ->
        BlockButton(
            label = "Quero me batizar",
            onClick = { onEnroll(found) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

// infoRow/infoIcon/infoLabel de app/batismo.tsx.
@Composable
private fun InfoRow(icon: String, label: String, content: @Composable ColumnScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            content()
        }
    }
}

@Composable
private fun InfoValue(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

// stepItem/stepCircle/stepText de app/batismo.tsx.
@Composable
private fun Step(number: Int, text: String) {
    Row(modifier = Modifier.padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 16.dp)
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Text(
            text = text,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

// btnPrimary de app/batismo.tsx: mais alto que o botão dos cartões, com a sombra azul.
@Composable
private fun ContactButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary,
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Falar com a Secretaria",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
