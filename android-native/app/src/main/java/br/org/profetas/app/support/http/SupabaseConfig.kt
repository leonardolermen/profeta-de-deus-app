package br.org.profetas.app.support.http

import br.org.profetas.app.BuildConfig

data class SupabaseConfig(val url: String, val anonKey: String) {

    val isUsable: Boolean get() = isSecureUrl(url) && anonKey.isNotBlank()

    companion object {
        fun fromBuildConfig(): SupabaseConfig = SupabaseConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
    }
}
