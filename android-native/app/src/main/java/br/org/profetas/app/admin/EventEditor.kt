package br.org.profetas.app.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.EventType
import br.org.profetas.app.support.storage.StorageBucket
import br.org.profetas.app.support.ui.AppTextArea
import br.org.profetas.app.support.ui.AppTextField
import java.time.ZoneId

private const val MAX_TITLE_INPUT = 160
private const val MAX_DESCRIPTION_INPUT = 4000

@Composable
fun EventEditor(eventId: String, viewModel: AdminViewModel, storageBaseUrl: String, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    EditorRecord(
        id = eventId,
        records = state.events,
        recordId = Event::id,
        hasLoaded = state.hasLoaded,
        failure = state.failure,
        onRetry = viewModel::reload,
    ) { event ->
        EventFields(
            event = event,
            isSaving = state.saving,
            storageBaseUrl = storageBaseUrl,
            onSave = { form, photo -> viewModel.saveEvent(event?.id, form, photo) },
        )
    }
}

@Composable
private fun EventFields(
    event: Event?,
    isSaving: Boolean,
    storageBaseUrl: String,
    onSave: (EventForm, PhotoIntent) -> Unit,
) {
    val zone = ZoneId.systemDefault()

    var title by rememberSaveable { mutableStateOf(event?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(event?.description.orEmpty()) }
    var date by rememberSaveable { mutableStateOf(event?.let { saved -> eventDateInput(saved.date, zone) }.orEmpty()) }
    var type by rememberSaveable { mutableStateOf(event?.type ?: EventType.CULTO) }
    var photo by rememberPhotoIntent()

    val storedUrl = event?.imagePath?.let { path -> StorageBucket.EVENTS.authenticatedUrl(storageBaseUrl, path) }

    EditorColumn(isNew = event == null) {
        AppTextField(label = "Título", value = title, onChange = { input -> title = input.take(MAX_TITLE_INPUT) })

        AppTextArea(
            label = "Descrição",
            value = description,
            onChange = { input -> description = input.take(MAX_DESCRIPTION_INPUT) },
        )

        AppTextField(
            label = "Data e hora",
            value = date,
            onChange = { input -> date = input },
            placeholder = "aaaa-MM-dd HH:mm",
        )

        FieldHint("Horário local: ${zone.id}. Será salvo com fuso horário.")

        ChoiceChips(
            label = "Tipo",
            options = EventType.entries,
            selected = type,
            optionLabel = EventType::label,
            onSelect = { selected -> type = selected },
        )

        PhotoPickerField(
            storedUrl = storedUrl,
            photo = photo,
            onChange = { change -> photo = change },
            isUploading = isSaving && photo is PhotoIntent.Replace,
            onRemove = { photo = PhotoIntent.Remove },
        )

        FieldHint("Sem foto, o card usa a imagem padrão do tipo de evento.")

        SaveButton(isSaving = isSaving) {
            val form = EventForm(
                title = title,
                description = description,
                date = date,
                type = type,
                imagePath = event?.imagePath,
            )

            onSave(form, photo)
        }
    }
}
