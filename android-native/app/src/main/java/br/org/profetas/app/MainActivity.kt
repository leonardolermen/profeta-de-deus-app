package br.org.profetas.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.org.profetas.app.navigation.ProfetasApp
import br.org.profetas.app.navigation.Route
import br.org.profetas.app.navigation.routeFromDebugLink

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val container = (application as ProfetasApplication).container

        // Recriada (rotação, tema), a Activity já tem a pilha de navegação restaurada e o link de recuperação
        // já consumido: abrir de novo a rota inicial ou reaplicar o link duplicaria a ação.
        val isFirstLaunch = savedInstanceState == null
        val link = intent.dataString.takeIf { isFirstLaunch }
        val initialRoute = if (isFirstLaunch) initialRoute(intent, link) else null
        val recoveryLink = link?.takeIf { data -> data.startsWith(RECOVERY_LINK_PREFIX) }

        setContent {
            ProfetasApp(container = container, initialRoute = initialRoute, recoveryLink = recoveryLink)
        }
    }

    // O lembrete de evento abre o app com o id no extra "event_id" (EventReminderWorker); o deep link de debug,
    // com profetas://open/<rota>.
    private fun initialRoute(intent: Intent, link: String?): Route? {
        val eventId = intent.getStringExtra("event_id")
        if (eventId != null) {
            return Route.EventDetail(eventId)
        }

        // O intent-filter do link de debug só existe no build debug, mas qualquer app pode mandar um intent explícito
        // com profetas://open/... para esta Activity: no release o roteador nem é consultado.
        if (!BuildConfig.DEBUG) {
            return null
        }

        return link?.let(::routeFromDebugLink)
    }

    private companion object {
        const val RECOVERY_LINK_PREFIX = "profetas://auth/"
    }
}
