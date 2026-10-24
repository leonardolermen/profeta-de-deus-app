package br.org.profetas.app.event.reminder

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import br.org.profetas.app.event.Event
import java.util.concurrent.TimeUnit

// Nome do trabalho único e chave nas preferências são os mesmos das versões anteriores: mudar qualquer um deixaria
// lembretes já agendados sem como cancelar e o membro sem saber quais estão ativos.
private const val REMINDERS_KEY = "reminders"

fun reminderWorkName(eventId: String): String = "event:$eventId"

class ReminderScheduler(context: Context, private val preferences: SharedPreferences) : EventReminders {

    private val workManager = WorkManager.getInstance(context)

    override fun schedule(event: Event) {
        val delay = reminderDelay(event.date) ?: return

        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInputData(workDataOf(EventReminderWorker.ID to event.id, EventReminderWorker.DATE to event.date))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniqueWork(reminderWorkName(event.id), ExistingWorkPolicy.REPLACE, request)

        save(enabledIds() + event.id)
    }

    override fun cancel(eventId: String) {
        workManager.cancelUniqueWork(reminderWorkName(eventId))

        save(enabledIds() - eventId)
    }

    // Cópia: o conjunto devolvido por getStringSet não pode ser alterado.
    override fun enabledIds(): Set<String> = preferences.getStringSet(REMINDERS_KEY, emptySet()).orEmpty().toSet()

    private fun save(ids: Set<String>) {
        preferences.edit {
            putStringSet(REMINDERS_KEY, ids)
        }
    }
}
