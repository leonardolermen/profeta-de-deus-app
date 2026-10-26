package br.org.profetas.app.admin

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.weekly.WeeklyService
import br.org.profetas.app.event.weekly.dayOfWeekFromDatabase
import br.org.profetas.app.event.weekly.shortLabel
import br.org.profetas.app.support.ui.AppTextField
import java.time.DayOfWeek

private const val MAX_TEXT_INPUT = 120

// Dom..Sáb, na ordem de dia_semana no banco.
private val WEEK_FROM_SUNDAY = (0 until 7).map(::dayOfWeekFromDatabase)

@Composable
fun WeeklyServiceEditor(serviceId: String, viewModel: AdminViewModel, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    EditorRecord(
        id = serviceId,
        records = state.weeklyServices,
        recordId = WeeklyService::id,
        hasLoaded = state.hasLoaded,
        failure = state.failure,
        onRetry = viewModel::reload,
    ) { service ->
        WeeklyServiceFields(
            service = service,
            isSaving = state.saving,
            onSave = { form -> viewModel.saveWeeklyService(service?.id, form) },
        )
    }
}

@Composable
private fun WeeklyServiceFields(service: WeeklyService?, isSaving: Boolean, onSave: (WeeklyServiceForm) -> Unit) {
    var name by rememberSaveable { mutableStateOf(service?.name.orEmpty()) }
    var dayOfWeek by rememberSaveable { mutableStateOf(service?.dayOfWeek ?: DayOfWeek.SUNDAY) }
    var times by rememberSaveable { mutableStateOf(service?.times?.joinToString(", ").orEmpty()) }
    var highlight by rememberSaveable { mutableStateOf(service?.highlight.orEmpty()) }
    var place by rememberSaveable { mutableStateOf(service?.place.orEmpty()) }
    var order by rememberSaveable { mutableStateOf(service?.order?.toString().orEmpty()) }

    EditorColumn(isNew = service == null) {
        AppTextField(label = "Nome", value = name, onChange = { input -> name = input.take(MAX_TEXT_INPUT) })

        ChoiceChips(
            label = "Dia",
            options = WEEK_FROM_SUNDAY,
            selected = dayOfWeek,
            optionLabel = DayOfWeek::shortLabel,
            onSelect = { selected -> dayOfWeek = selected },
        )

        AppTextField(
            label = "Horários",
            value = times,
            onChange = { input -> times = input.take(MAX_TEXT_INPUT) },
            placeholder = "08:00, 15:00, 18:00",
        )

        FieldHint("Separe os horários por vírgula; a lista é gravada em ordem.")

        AppTextField(
            label = "Destaque",
            value = highlight,
            onChange = { input -> highlight = input.take(MAX_TEXT_INPUT) },
            placeholder = "Ex: Santa Ceia no primeiro domingo",
        )

        AppTextField(
            label = "Local",
            value = place,
            onChange = { input -> place = input.take(MAX_TEXT_INPUT) },
            placeholder = "Ex: Templo sede",
        )

        AppTextField(
            label = "Ordem",
            value = order,
            onChange = { input -> order = input.take(3) },
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        SaveButton(isSaving = isSaving) {
            val form = WeeklyServiceForm(
                name = name,
                dayOfWeek = dayOfWeek,
                times = times,
                highlight = highlight,
                place = place,
                order = order,
            )

            onSave(form)
        }
    }
}
