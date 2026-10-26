package br.org.profetas.app.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.church.Church
import br.org.profetas.app.support.ui.AppTextArea
import br.org.profetas.app.support.ui.AppTextField
import br.org.profetas.app.support.ui.LoadFailure

private const val MAX_SHORT_INPUT = 1000
private const val MAX_LONG_INPUT = 50000

// A igreja tem um registro só, que sempre existe: o editor espera a carga para não abrir com os valores padrão e
// gravá-los por cima dos verdadeiros.
@Composable
fun ChurchEditor(viewModel: AdminViewModel, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    val failure = state.failure
    if (!state.hasLoaded && failure != null) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            LoadFailure(message = failure, onRetry = viewModel::reload)
        }

        return
    }

    if (!state.hasLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        return
    }

    key(state.church) {
        ChurchFields(church = state.church, isSaving = state.saving, onSave = viewModel::saveChurch)
    }
}

@Composable
private fun ChurchFields(church: Church, isSaving: Boolean, onSave: (ChurchForm) -> Unit) {
    var name by rememberSaveable { mutableStateOf(church.name) }
    var phone by rememberSaveable { mutableStateOf(church.phone) }
    var whatsapp by rememberSaveable { mutableStateOf(church.whatsapp) }
    var email by rememberSaveable { mutableStateOf(church.email) }
    var address by rememberSaveable { mutableStateOf(church.address) }
    var history by rememberSaveable { mutableStateOf(church.history) }
    var privacy by rememberSaveable { mutableStateOf(church.privacy) }

    EditorColumn(isNew = false) {
        AppTextField(
            label = "Nome da igreja",
            value = name,
            onChange = { input -> name = input.take(MAX_SHORT_INPUT) },
        )

        AppTextField(
            label = "Telefone com DDD",
            value = phone,
            onChange = { input -> phone = input.take(MAX_SHORT_INPUT) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        AppTextField(
            label = "WhatsApp com país e DDD (55…)",
            value = whatsapp,
            onChange = { input -> whatsapp = input.take(MAX_SHORT_INPUT) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        AppTextField(
            label = "E-mail",
            value = email,
            onChange = { input -> email = input.take(MAX_SHORT_INPUT) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        AppTextField(
            label = "Endereço completo",
            value = address,
            onChange = { input -> address = input.take(MAX_SHORT_INPUT) },
        )

        AppTextArea(
            label = "História e missão",
            value = history,
            onChange = { input -> history = input.take(MAX_LONG_INPUT) },
        )

        AppTextArea(
            label = "Política de privacidade e canal para solicitações",
            value = privacy,
            onChange = { input -> privacy = input.take(MAX_LONG_INPUT) },
        )

        SaveButton(isSaving = isSaving) {
            val form = ChurchForm(
                name = name,
                phone = phone,
                whatsapp = whatsapp,
                email = email,
                address = address,
                history = history,
                privacy = privacy,
            )

            onSave(form)
        }
    }
}
