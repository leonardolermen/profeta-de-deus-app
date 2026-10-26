package br.org.profetas.app.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.support.ui.EmptyState
import br.org.profetas.app.support.ui.LoadFailure

// Peças comuns aos editores da Administração: mesma coluna, mesmo cabeçalho e o mesmo botão de salvar.

@Composable
fun EditorColumn(isNew: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = if (isNew) "Novo cadastro" else "Editar cadastro",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        content()
    }
}

// O registro aberto vem da lista recarregada do AdminViewModel. Aberto direto (link de debug), a lista ainda está
// vazia: espera a carga em vez de dizer que o registro não existe. Se a carga falhou, oferece tentar de novo em vez
// de girar para sempre.
@Composable
fun <T> EditorRecord(
    id: String,
    records: List<T>,
    recordId: (T) -> String,
    hasLoaded: Boolean,
    failure: String?,
    onRetry: () -> Unit,
    content: @Composable (T?) -> Unit,
) {
    if (id == Route.NEW_RECORD) {
        content(null)

        return
    }

    val record = records.firstOrNull { candidate -> recordId(candidate) == id }

    if (record != null) {
        key(id) {
            content(record)
        }
    } else if (!hasLoaded && failure != null) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            LoadFailure(message = failure, onRetry = onRetry)
        }
    } else if (!hasLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        EmptyState("Registro indisponível. Volte e atualize os dados.")
    }
}

// Fecha o editor quando a gravação termina; o aviso "Cadastro salvo." já sai pelo snackbar.
@Composable
fun SavedEffect(viewModel: AdminViewModel, onSaved: () -> Unit) {
    LaunchedEffect(viewModel) {
        viewModel.saved.collect {
            onSaved()
        }
    }
}

@Composable
fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(if (isSaving) 0.7f else 1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = !isSaving, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                text = "Salvar cadastro",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

// Sem isso o chip marcado sai no lilás padrão do Material, fora da paleta da igreja (como na aba Eventos).
@Composable
fun churchChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.primary,
)

@Composable
fun <T> ChoiceChips(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp),
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(optionLabel(option)) },
                    colors = churchChipColors(),
                )
            }
        }
    }
}

@Composable
fun FieldHint(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp),
    )
}
