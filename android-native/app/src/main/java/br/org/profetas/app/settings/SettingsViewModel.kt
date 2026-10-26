package br.org.profetas.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.event.reminder.EventReminders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val theme: ThemePreference,
    private val reminders: EventReminders,
) : ViewModel() {

    val isDark: StateFlow<Boolean> get() = theme.isDark

    private val mutableReminderCount = MutableStateFlow(reminders.enabledIds().size)

    val reminderCount: StateFlow<Int> = mutableReminderCount.asStateFlow()

    fun useDark() {
        theme.useDark()
    }

    fun useLight() {
        theme.useLight()
    }

    // Os lembretes são ligados na tela do evento: ao voltar para os Ajustes a contagem precisa ser relida.
    fun reload() {
        mutableReminderCount.value = reminders.enabledIds().size
    }

    fun cancelAllReminders() {
        reminders.enabledIds().forEach(reminders::cancel)

        reload()
    }

    companion object {

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ProfetasApplication

                val container = application.container

                SettingsViewModel(theme = container.theme, reminders = container.reminders)
            }
        }
    }
}
