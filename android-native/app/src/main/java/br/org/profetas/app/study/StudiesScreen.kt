package br.org.profetas.app.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.BorderedCard
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure

// Estudos, a partir de app/estudos.tsx: as medidas são as do StyleSheet de lá. O Expo tinha três estudos fixos e o
// tempo de cada um ("4 Lições"); aqui os estudos vêm do banco, que não guarda tempo, e no lugar dele aparece se o
// membro já concluiu.
@Composable
fun StudiesScreen(viewModel: StudiesViewModel, onNavigate: (Route) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 80.dp),
    ) {
        Text(
            text = "Trilhas e Estudos",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Aprofunde seu conhecimento na Palavra de Deus com nossos materiais preparados pelos pastores.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        state.failure?.let { failure ->
            LoadFailure(message = failure, onRetry = viewModel::reload, modifier = Modifier.padding(bottom = 16.dp))
        }

        when {
            state.studies.isNotEmpty() -> state.studies.forEach { study ->
                StudyCard(
                    study = study,
                    isCompleted = study.id in state.completedIds,
                    onOpen = { onNavigate(Route.Study(study.id)) },
                )
            }

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            else -> EmptyState("Nenhum estudo publicado ainda.")
        }
    }
}

@Composable
private fun StudyCard(study: Study, isCompleted: Boolean, onOpen: () -> Unit) {
    BorderedCard(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (study.category.isNotBlank()) {
                    CategoryBadge(study.category)
                }
            }

            if (isCompleted) {
                Text(
                    text = "✓ Concluído",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = study.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (study.description.isNotBlank()) {
            Text(
                text = study.description,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isCompleted) "Rever estudo" else "Começar estudo agora",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )

            Text(text = "→", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// badge/badgeText do Expo: categoria em maiúsculas sobre o azul primário.
@Composable
internal fun CategoryBadge(category: String) {
    Text(
        text = category.uppercase(),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
