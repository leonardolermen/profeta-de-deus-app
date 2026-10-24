package br.org.profetas.app.birthday

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

// Aniversariantes, a partir de app/aniversariantes.tsx: as medidas são as do StyleSheet de lá. O Expo tinha uma
// lista fixa de um só mês; aqui os aniversariantes vêm do banco (a RLS só devolve quem está logado e deu
// consentimento) e o seletor de mês da tela Kotlin anterior continua, para ver outros meses.
@Composable
fun BirthdaysScreen(viewModel: BirthdaysViewModel, isLoggedIn: Boolean, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var month by rememberSaveable { mutableIntStateOf(LocalDate.now().monthValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        Header(month)

        if (!isLoggedIn) {
            EmptyState("Entre para consultar os aniversariantes que autorizaram a divulgação.")

            BlockButton(label = "Entrar", onClick = { onNavigate(Route.Login) })

            return@Column
        }

        MonthSelector(month = month, onSelect = { selected -> month = selected })

        state.failure?.let { failure ->
            LoadFailure(
                message = failure,
                onRetry = viewModel::reload,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        val birthdays = viewModel.byMonth(month)

        when {
            birthdays.isNotEmpty() -> BirthdayList(birthdays)

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            else -> EmptyState("Nenhum aniversariante publicado neste mês.")
        }
    }
}

@Composable
private fun Header(month: Int) {
    val monthName = Month.of(month)
        .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
        .replaceFirstChar { character -> character.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎂", fontSize = 54.sp, modifier = Modifier.padding(bottom = 8.dp))

        Text(
            text = "Parabéns!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Celebre a vida dos nossos irmãos que completam mais um ano de vida no mês de $monthName.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

@Composable
private fun MonthSelector(month: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (1..12).forEach { candidate ->
            val label = Month.of(candidate).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"))

            FilterChip(selected = month == candidate, onClick = { onSelect(candidate) }, label = { Text(label) })
        }
    }
}

// listWrapper de app/aniversariantes.tsx: cartão com borda e cada linha separada por um filete, exceto a última.
@Composable
private fun BirthdayList(items: List<Birthday>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
    ) {
        items.forEachIndexed { index, birthday ->
            BirthdayRow(birthday)

            if (index != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )
            }
        }
    }
}

@Composable
private fun BirthdayRow(birthday: Birthday) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }

            Text(
                text = birthday.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "Dia ${birthday.day.toString().padStart(2, '0')}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
