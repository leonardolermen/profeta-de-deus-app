package br.org.profetas.app.navigation

import java.net.URI

// Toda tela do app. `path` é o destino concreto que se navega; `pattern` é o molde registrado no NavHost
// (igual ao path nas telas sem argumento).
sealed interface Route {

    val path: String

    val title: String

    val pattern: String get() = path

    data object Home : Route {
        override val path = "home"
        override val title = "Início"
    }

    data object Bible : Route {
        override val path = "bible"
        override val title = "Bíblia"
    }

    data object Events : Route {
        override val path = "events"
        override val title = "Eventos"
    }

    data object Settings : Route {
        override val path = "settings"
        override val title = "Ajustes"
    }

    data object Prayer : Route {
        override val path = "prayer"
        override val title = "Oração"
    }

    data object Campaigns : Route {
        override val path = "campaigns"
        override val title = "Projetos"
    }

    data object Baptism : Route {
        override val path = "baptism"
        override val title = "Batismo"
    }

    data object Birthdays : Route {
        override val path = "birthdays"
        override val title = "Aniversariantes"
    }

    data object Studies : Route {
        override val path = "studies"
        override val title = "Estudos"
    }

    data class Study(val id: String) : Route {
        override val path = "study/$id"
        override val title = "Estudo"
        override val pattern = PATTERN

        companion object {
            const val PATTERN = "study/{id}"
        }
    }

    data object Gallery : Route {
        override val path = "gallery"
        override val title = "Galeria"
    }

    data class Photo(val id: String) : Route {
        override val path = "photo/$id"
        override val title = "Foto"
        override val pattern = PATTERN

        companion object {
            const val PATTERN = "photo/{id}"
        }
    }

    data object History : Route {
        override val path = "history"
        override val title = "Nossa História"
    }

    data object Contact : Route {
        override val path = "contact"
        override val title = "Contato"
    }

    data object Privacy : Route {
        override val path = "privacy"
        override val title = "Privacidade"
    }

    data object Login : Route {
        override val path = "login"
        override val title = "Entrar"
    }

    data object Register : Route {
        override val path = "register"
        override val title = "Criar conta"
    }

    data object Recovery : Route {
        override val path = "recovery"
        override val title = "Recuperar acesso"
    }

    data object Profile : Route {
        override val path = "profile"
        override val title = "Meu perfil"
    }

    data class EventDetail(val id: String) : Route {
        override val path = "event/$id"
        override val title = "Evento"
        override val pattern = PATTERN

        companion object {
            const val PATTERN = "event/{id}"
        }
    }

    data object MyRegistrations : Route {
        override val path = "registrations"
        override val title = "Minhas inscrições"
    }

    data object Admin : Route {
        override val path = "admin"
        override val title = "Administração"
    }

    data class Editor(val section: String, val id: String) : Route {
        override val path = "editor/$section/$id"
        override val title = "Editar"
        override val pattern = PATTERN

        companion object {
            const val PATTERN = "editor/{section}/{id}"
        }
    }

    companion object {

        // As quatro abas do app Expo (app/(tabs)/_layout.tsx), na mesma ordem.
        val tabs: List<Route> = listOf(Home, Bible, Events, Settings)

        private val withoutArguments: List<Route> = listOf(
            Home, Bible, Events, Settings, Prayer, Campaigns, Baptism, Birthdays, Studies, Gallery, History,
            Contact, Privacy, Login, Register, Recovery, Profile, MyRegistrations, Admin,
        )

        fun fromPath(path: String): Route? {
            val segments = path.trim('/').split('/')

            return when (segments.size) {
                1 -> withoutArguments.firstOrNull { route -> route.path == segments[0] }
                2 -> withOneArgument(segments[0], segments[1])
                3 -> if (segments[0] == "editor") Editor(segments[1], segments[2]) else null
                else -> null
            }
        }

        private fun withOneArgument(name: String, id: String): Route? = when (name) {
            "event" -> EventDetail(id)
            "study" -> Study(id)
            "photo" -> Photo(id)
            else -> null
        }
    }
}

// Deep link só de debug (profetas://open/<rota>, ver src/debug/AndroidManifest.xml): abre qualquer tela direto
// para a conferência visual no emulador. No release o filtro não existe, então nenhum link chega aqui.
fun routeFromDebugLink(link: String): Route? {
    val uri = runCatching { URI(link) }.getOrNull() ?: return null

    val isDebugLink = uri.scheme == "profetas" && uri.host == "open"
    if (!isDebugLink) {
        return null
    }

    return Route.fromPath(uri.path.orEmpty())
}
