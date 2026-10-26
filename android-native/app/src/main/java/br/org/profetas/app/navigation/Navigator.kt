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

    // Entrou: sai do login junto com o cadastro que talvez esteja por cima, em vez de devolver a quem acabou de
    // entrar a tela de login. Aberto direto pelo link (sem login na pilha), basta fechar a tela atual.
    fun finishSignIn() {
        val leftLogin = navController.popBackStack(Route.Login.path, inclusive = true)
        if (!leftLogin) {
            navController.popBackStack()
        }
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
