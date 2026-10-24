package br.org.profetas.app.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

class Navigator(private val navController: NavController) {

    fun go(route: Route) {
        if (route in Route.tabs) {
            goToTab(route)
        } else {
            navController.navigate(route.path) {
                launchSingleTop = true
            }
        }
    }

    fun back() {
        navController.popBackStack()
    }

    // As telas antigas de ui/ ainda navegam por string; "auth" era a tela única de conta, hoje Route.Login.
    fun goLegacyPath(path: String) {
        val route = if (path == "auth") Route.Login else Route.fromPath(path)

        route?.let(::go)
    }

    // Trocar de aba volta para a raiz e guarda a pilha da aba que sai, como a barra de abas do Expo.
    private fun goToTab(tab: Route) {
        navController.navigate(tab.path) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
