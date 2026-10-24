package br.org.profetas.app.support.http

import java.net.URI

/** Verifica se [value] é uma URL https de host real, sem credenciais embutidas na URL. */
fun isSecureUrl(value: String): Boolean = runCatching {
    val uri = URI(value)
    uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null
}.getOrDefault(false)
