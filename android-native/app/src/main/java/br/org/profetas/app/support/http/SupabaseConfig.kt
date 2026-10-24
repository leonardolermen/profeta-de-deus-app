package br.org.profetas.app.support.http

import br.org.profetas.app.BuildConfig
import java.net.URI

private val LOCAL_EMULATOR_HOSTS = setOf("10.0.2.2", "127.0.0.1")

data class SupabaseConfig(val url: String, val anonKey: String, val allowLocalHttp: Boolean) {

    val isUsable: Boolean get() = (isSecureUrl(url) || (allowLocalHttp && isLocalEmulatorUrl())) && anonKey.isNotBlank()

    // Emulador Android expõe o host da máquina em 10.0.2.2; 127.0.0.1 cobre o caminho com adb reverse.
    // Só libera http para esses hosts, e só quando allowLocalHttp vem de um build debug.
    private fun isLocalEmulatorUrl(): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "http" && uri.host in LOCAL_EMULATOR_HOSTS && uri.userInfo == null
    }.getOrDefault(false)

    companion object {
        fun fromBuildConfig(): SupabaseConfig = SupabaseConfig(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_ANON_KEY,
            BuildConfig.ALLOW_LOCAL_HTTP,
        )
    }
}
