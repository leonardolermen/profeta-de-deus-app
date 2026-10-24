package br.org.profetas.app.support.session

data class Session(val access: String, val refresh: String, val userId: String, val email: String)
