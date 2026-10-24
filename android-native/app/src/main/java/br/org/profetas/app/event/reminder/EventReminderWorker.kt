package br.org.profetas.app.event.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.org.profetas.app.MainActivity
import br.org.profetas.app.ProfetasApplication
import br.org.profetas.app.R
import br.org.profetas.app.event.Event
import br.org.profetas.app.event.displayDate
import br.org.profetas.app.event.isFuture
import kotlinx.coroutines.CancellationException

private const val CHANNEL_ID = "eventos"

private const val MAX_ATTEMPTS = 3

class EventReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val isNotificationAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!isNotificationAllowed) {
            return Result.success()
        }

        val id = inputData.getString(ID) ?: return Result.failure()

        val container = (applicationContext as ProfetasApplication).container
        if (id !in container.reminders.enabledIds()) {
            return Result.success()
        }

        // Revalida no servidor: nao notifica evento cancelado ou com data alterada.
        val event = try {
            container.events.all().find { candidate -> candidate.id == id }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }

            return if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }

        if (event == null || event.date != inputData.getString(DATE) || !isFuture(event.date)) {
            return Result.success()
        }

        showNotification(event)

        return Result.success()
    }

    private fun showNotification(event: Event) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Lembretes de eventos", NotificationManager.IMPORTANCE_DEFAULT),
        )

        // O MainActivity abre o detalhe do evento a partir deste extra.
        val intent = Intent(applicationContext, MainActivity::class.java).putExtra("event_id", event.id)
        val pending = PendingIntent.getActivity(
            applicationContext,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(event.title)
            .setContentText(displayDate(event.date))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        // A permissão pode ter sido revogada entre a checagem e aqui; sem ela não há o que mostrar.
        try {
            NotificationManagerCompat.from(applicationContext).notify(event.id.hashCode(), notification)
        } catch (e: SecurityException) {
            return
        }
    }

    companion object {
        const val ID = "id"
        const val DATE = "date"
    }
}
