package br.org.profetas.app.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.support.intent.share
import br.org.profetas.app.support.ui.BlockButton
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure

// Leitura de um estudo. O Expo não tinha esta tela (o cartão não abria nada); ela segue o desenho de
// app/estudos.tsx — título, badge da categoria, descrição — e traz concluir e compartilhar da tela Kotlin anterior.
@Composable
fun StudyScreen(studyId: String, viewModel: StudiesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfEmpty()
    }

    val study = state.studies.firstOrNull { candidate -> candidate.id == studyId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        when {
            study != null -> StudyContent(
                study = study,
                isCompleted = study.id in state.completedIds,
                onToggleCompleted = { viewModel.toggleCompleted(study.id) },
            )

            state.loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally),
            )

            state.failure != null -> LoadFailure(message = state.failure.orEmpty(), onRetry = viewModel::reload)

            else -> EmptyState("Estudo indisponível.")
        }
    }
}

@Composable
private fun StudyContent(study: Study, isCompleted: Boolean, onToggleCompleted: () -> Unit) {
    val context = LocalContext.current

    if (study.category.isNotBlank()) {
        Box(modifier = Modifier.padding(bottom = 12.dp)) {
            CategoryBadge(study.category)
        }
    }

    Text(
        text = study.title,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
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

    Text(
        text = study.content,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 20.dp),
    )

    BlockButton(
        label = if (isCompleted) "Marcar como não concluído" else "Concluir estudo",
        onClick = onToggleCompleted,
        modifier = Modifier.padding(bottom = 12.dp),
    )

    OutlineButton(
        label = "Compartilhar estudo",
        onClick = { share(context, "${study.title}\n\n${study.content}") },
    )
}

// Botão secundário com o mesmo formato do BlockButton, em contorno, para não competir com a ação principal.
@Composable
private fun OutlineButton(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)

    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
    )
}
