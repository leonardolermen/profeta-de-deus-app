package br.org.profetas.app.bible

data class Verse(val number: Int, val text: String)

data class Chapter(val book: String, val number: Int, val verses: List<Verse>)
