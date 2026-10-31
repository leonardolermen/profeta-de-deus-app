package br.org.profetas.app.admin

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.org.profetas.app.support.ui.AppTextField
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

private val DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

@Composable
fun EventDateFields(day: String, time: String, onDay: (String) -> Unit, onTime: (String) -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var selecting by rememberSaveable { mutableStateOf(false) }
    AppTextField("Data do evento", day, { onDay(formatEventDayInput(it)) }, placeholder = "dd/MM/aaaa",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    OutlinedButton(onClick = { selecting = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Escolher no calendário") }
    if (selecting) DisposableEffect(context, configuration) {
        // Contexto local: meses em português mesmo quando o telefone está em outro idioma.
        val config = Configuration(configuration).apply { setLocale(Locale.forLanguageTag("pt-BR")) }
        val localized = android.view.ContextThemeWrapper(context, 0).apply { applyOverrideConfiguration(config) }
        val selected = runCatching { LocalDate.parse(day, DAY_FORMAT) }.getOrElse { LocalDate.now() }
        val dialog = DatePickerDialog(localized, { _, year, month, date ->
            onDay(LocalDate.of(year, month + 1, date).format(DAY_FORMAT))
        }, selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
            setTitle("Escolher data")
            setButton(DatePickerDialog.BUTTON_POSITIVE, "Confirmar", this)
            setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", this)
            setOnDismissListener { selecting = false }
        }
        dialog.show()
        onDispose { dialog.setOnDismissListener(null); dialog.dismiss() }
    }
    AppTextField("Horário do evento", time, { onTime(formatEventTimeInput(it)) }, placeholder = "Ex.: 19:30",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    FieldHint("Use o formato de 24 horas: 08:00 pela manhã ou 19:30 à noite.")
}
