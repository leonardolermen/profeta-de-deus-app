package br.org.profetas.app.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.org.profetas.app.study.Study
import br.org.profetas.app.support.ui.AppTextArea
import br.org.profetas.app.support.ui.AppTextField

private const val MAX_TITLE_INPUT = 160
private const val MAX_SHORT_INPUT = 1000
private const val MAX_SUMMARY_INPUT = 4000
private const val MAX_CONTENT_INPUT = 50000

@Composable
fun StudyEditor(studyId: String, viewModel: AdminViewModel, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SavedEffect(viewModel, onSaved)

    EditorRecord(
        id = studyId,
        records = state.studies,
        recordId = Study::id,
        hasLoaded = state.hasLoaded,
        failure = state.failure,
        onRetry = viewModel::reload,
    ) { study ->
        StudyFields(
            study = study,
            isSaving = state.saving,
            onSave = { form -> viewModel.saveStudy(study?.id, form) },
        )
    }
}

@Composable
private fun StudyFields(study: Study?, isSaving: Boolean, onSave: (StudyForm) -> Unit) {
    var title by rememberSaveable { mutableStateOf(study?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(study?.description.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(study?.category.orEmpty()) }
    var content by rememberSaveable { mutableStateOf(study?.content.orEmpty()) }

    EditorColumn(isNew = study == null) {
        AppTextField(label = "Título", value = title, onChange = { input -> title = input.take(MAX_TITLE_INPUT) })

        AppTextArea(
            label = "Resumo",
            value = description,
            onChange = { input -> description = input.take(MAX_SUMMARY_INPUT) },
        )

        AppTextField(
            label = "Categoria",
            value = category,
            onChange = { input -> category = input.take(MAX_SHORT_INPUT) },
        )

        AppTextArea(
            label = "Conteúdo completo",
            value = content,
            onChange = { input -> content = input.take(MAX_CONTENT_INPUT) },
        )

        SaveButton(isSaving = isSaving) {
            onSave(StudyForm(title = title, description = description, category = category, content = content))
        }
    }
}
