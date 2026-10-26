package br.org.profetas.app.support.http

import java.net.URLEncoder

// URLEncoder segue o formato de formulário (espaço vira "+"), mas em caminho e em consulta de URL o "+" é literal.
fun encodeUrlComponent(text: String): String = URLEncoder.encode(text, Charsets.UTF_8.name()).replace("+", "%20")
