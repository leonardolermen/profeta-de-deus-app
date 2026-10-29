package br.org.profetas.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class ProfetasApplication : Application(), ImageLoaderFactory {

    // Lazy: o AppContainer lê a sessão do Keystore, o que só precisa acontecer quando a primeira tela
    // ou o lembrete de evento pedir.
    val container: AppContainer by lazy { AppContainer(this) }
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { container.imageHttpClient() }
        .diskCache(null)
        .build()
}
