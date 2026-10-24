package br.org.profetas.app

import android.app.Application

class ProfetasApplication : Application() {

    // Lazy: o AppContainer lê a sessão do Keystore, o que só precisa acontecer quando a primeira tela
    // ou o lembrete de evento pedir.
    val container: AppContainer by lazy { AppContainer(this) }
}
