package br.org.profetas.app.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.birthday.Birthday
import br.org.profetas.app.support.ui.AppTextField

private const val MAX_NAME_INPUT = 120

@Composable
fun BirthdayEditor(birthdayId: String, viewModel: AdminViewModel, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    EditorRecord(
        id = birthdayId,
        records = state.birthdays,
        recordId = Birthday::id,
        hasLoaded = state.hasLoaded,
        failure = state.failure,
        onRetry = viewModel::reload,
    ) { birthday ->
        BirthdayFields(
            birthday = birthday,
            isSaving = state.saving,
            onSave = { form -> viewModel.saveBirthday(birthday?.id, form) },
        )
    }
}

@Composable
private fun BirthdayFields(birthday: Birthday?, isSaving: Boolean, onSave: (BirthdayForm) -> Unit) {
    var name by rememberSaveable { mutableStateOf(birthday?.name.orEmpty()) }
    var day by rememberSaveable { mutableStateOf(birthday?.day?.toString().orEmpty()) }
    var month by rememberSaveable { mutableStateOf(birthday?.month?.toString().orEmpty()) }
    var consent by rememberSaveable { mutableStateOf(birthday?.consent ?: false) }

    val numbers = KeyboardOptions(keyboardType = KeyboardType.Number)

    EditorColumn(isNew = birthday == null) {
        AppTextField(label = "Nome", value = name, onChange = { input -> name = input.take(MAX_NAME_INPUT) })

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(
                label = "Dia",
                value = day,
                onChange = { input -> day = input.take(2) },
                keyboardOptions = numbers,
                modifier = Modifier.weight(1f),
            )

            AppTextField(
                label = "Mês (1 a 12)",
                value = month,
                onChange = { input -> month = input.take(2) },
                keyboardOptions = numbers,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { consent = !consent },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = consent, onCheckedChange = { checked -> consent = checked })

            Text(
                text = "A pessoa autorizou a divulgação do nome e dia/mês aos membros.",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        SaveButton(isSaving = isSaving) {
            onSave(BirthdayForm(name = name, day = day, month = month, consent = consent))
        }
    }
}
