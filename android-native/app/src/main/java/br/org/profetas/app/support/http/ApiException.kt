package br.org.profetas.app.support.http

class ApiException(val status: Int, message: String) : Exception(message)
