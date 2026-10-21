package br.org.profetas.app

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
import androidx.work.*
import br.org.profetas.app.data.*
import java.util.concurrent.TimeUnit

class EventReminder(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val id = inputData.getString("id") ?: return Result.failure()
        val enabled = applicationContext.getSharedPreferences("preferences", 0).getStringSet("reminders", emptySet()).orEmpty()
        if (id !in enabled) return Result.success()
        val repository = Repository(applicationContext)
        // Revalida no servidor: nao notifica evento cancelado ou com data alterada.
        val event = try { repository.events().find { it.id == id } } catch (_: Exception) { return if (runAttemptCount < 3) Result.retry() else Result.failure() }
        if (event == null || event.date != inputData.getString("date") || !isFuture(event.date)) return Result.success()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("eventos", "Lembretes de eventos", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(applicationContext, MainActivity::class.java).putExtra("event_id", id)
        val pending = PendingIntent.getActivity(applicationContext, id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, "eventos").setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(event.title).setContentText(displayDate(event.date)).setContentIntent(pending).setAutoCancel(true).build()
        try { NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification) } catch (_: SecurityException) { return Result.success() }
        return Result.success()
    }
    companion object {
        fun schedule(context: Context, event: Event) {
            val delay = reminderDelay(event.date) ?: return
            val request = OneTimeWorkRequestBuilder<EventReminder>().setInputData(workDataOf("id" to event.id, "date" to event.date))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
            WorkManager.getInstance(context).enqueueUniqueWork("event:${event.id}", ExistingWorkPolicy.REPLACE, request)
        }
        fun cancel(context: Context, id: String) { WorkManager.getInstance(context).cancelUniqueWork("event:$id") }
    }
}
